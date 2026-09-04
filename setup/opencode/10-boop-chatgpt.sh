#!/usr/bin/env bash
# BOOP -> Wyoming -> OpenCode bridge.
# Copy this single file to /config/opencode/startup.d/10-boop-chatgpt.sh
# after enabling OpenCode LAN server + Startup hooks. Do NOT map 4096/tcp.

set -euo pipefail

BASE=/data/boop-wyoming
VENV=/data/venvs/boop-wyoming
BRIDGE="$BASE/bridge.py"
REQS="$BASE/requirements.txt"
REQ_HASH="$VENV/.boop-requirements.sha256"
PID=/data/boop-wyoming/bridge.pid
LOG=/data/boop-wyoming/bridge.log
OPENCODE=http://127.0.0.1:4096
WYOMING_PORT=10400

mkdir -p "$BASE" "$(dirname "$VENV")"

# A manual `ha-hooks run` must not spawn a second bridge.
if [ -s "$PID" ]; then
    existing_pid="$(cat "$PID" 2>/dev/null || true)"
    if [ -n "$existing_pid" ] && kill -0 "$existing_pid" 2>/dev/null; then
        echo "BOOP Wyoming bridge already running as PID $existing_pid"
        exit 0
    fi
    rm -f "$PID"
fi

# The OpenCode add-on's server is internal-only here. Wait briefly for its
# supervised service to be ready; no Supervisor host-port mapping is needed.
ready=false
for _ in $(seq 1 30); do
    if python3 -c "import urllib.request; urllib.request.urlopen('$OPENCODE/global/health', timeout=1).read()" >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 1
done
if [ "$ready" != true ]; then
    echo "BOOP bridge: OpenCode did not become ready at 127.0.0.1:4096" >&2
    exit 1
fi

cat >"$REQS" <<'REQ'
aiohttp==3.14.3
wyoming==1.10.0
REQ

if [ ! -x "$VENV/bin/python" ]; then
    rm -rf "$VENV"
    python3 -m venv "$VENV"
fi

wanted_hash="$(sha256sum "$REQS" | awk '{print $1}')"
current_hash="$(cat "$REQ_HASH" 2>/dev/null || true)"
if [ "$wanted_hash" != "$current_hash" ]; then
    "$VENV/bin/python" -m pip install --disable-pip-version-check -r "$REQS"
    printf '%s\n' "$wanted_hash" >"$REQ_HASH"
fi

cat >"$BRIDGE" <<'PY'
#!/usr/bin/env python3
"""BOOP Wyoming conversation bridge to an already-authenticated OpenCode server."""

from __future__ import annotations

import argparse
import asyncio
import logging
from typing import Any, Awaitable, Callable, Iterable

import aiohttp
from wyoming.asr import Transcript
from wyoming.handle import Handled, NotHandled
from wyoming.info import Attribution, Describe, HandleProgram, Info
from wyoming.server import AsyncEventHandler, AsyncTcpServer

_LOGGER = logging.getLogger("boop-opencode-bridge")
DEFAULT_OPENCODE_URL = "http://127.0.0.1:4096"
DEFAULT_HOST = "0.0.0.0"
DEFAULT_PORT = 10400
OPENCODE_TIMEOUT_SECONDS = 45


def disabled_tools(tool_ids: Iterable[str]) -> dict[str, bool]:
    """Disable every tool OpenCode reports for BOOP's chat-only fallback."""
    return {str(tool_id): False for tool_id in tool_ids}


def extract_text(response: dict[str, Any]) -> str:
    """Extract final assistant text from an OpenCode message response."""
    parts = response.get("parts")
    if not isinstance(parts, list):
        raise ValueError("OpenCode response has no parts")

    text_parts: list[str] = []
    for part in parts:
        if not isinstance(part, dict) or part.get("type") != "text":
            continue
        text = part.get("text")
        if isinstance(text, str) and text.strip():
            text_parts.append(text.strip())

    if not text_parts:
        raise ValueError("OpenCode response contained no text")
    return "\n".join(text_parts)


class SessionRegistry:
    """Map Home Assistant conversation ids to OpenCode sessions."""

    def __init__(self) -> None:
        self._sessions: dict[str, str] = {}
        self._lock = asyncio.Lock()

    async def session_for(
        self,
        conversation_id: str | None,
        create_session: Callable[[], Awaitable[str]],
    ) -> str:
        if not conversation_id:
            return await create_session()

        async with self._lock:
            existing = self._sessions.get(conversation_id)
            if existing:
                return existing
            created = await create_session()
            self._sessions[conversation_id] = created
            return created


class OpenCodeClient:
    """Small HTTP client for the OpenCode server running in the same add-on."""

    def __init__(self, base_url: str = DEFAULT_OPENCODE_URL) -> None:
        self.base_url = base_url.rstrip("/")
        self.timeout = aiohttp.ClientTimeout(total=OPENCODE_TIMEOUT_SECONDS)

    async def _json(self, method: str, path: str, **kwargs: Any) -> Any:
        async with aiohttp.ClientSession(timeout=self.timeout) as session:
            async with session.request(method, self.base_url + path, **kwargs) as response:
                response.raise_for_status()
                if response.status == 204:
                    return None
                return await response.json()

    async def create_session(self) -> str:
        data = await self._json("POST", "/session", json={"title": "BOOP voice"})
        if not isinstance(data, dict):
            raise ValueError("OpenCode did not return a session object")
        session_id = data.get("id")
        if not isinstance(session_id, str) or not session_id:
            raise ValueError("OpenCode session has no id")
        return session_id

    async def tool_ids(self) -> list[str]:
        data = await self._json("GET", "/experimental/tool/ids")
        if isinstance(data, list):
            return [str(item) for item in data]
        if isinstance(data, dict):
            for key in ("ids", "tools", "data"):
                value = data.get(key)
                if isinstance(value, list):
                    return [str(item) for item in value]
        raise ValueError("OpenCode tool list had an unexpected shape")

    async def prompt(self, session_id: str, text: str) -> str:
        tool_map = disabled_tools(await self.tool_ids())
        body = {
            "agent": "general",
            "tools": tool_map,
            "parts": [{"type": "text", "text": text}],
        }
        data = await self._json("POST", f"/session/{session_id}/message", json=body)
        if not isinstance(data, dict):
            raise ValueError("OpenCode message response was not an object")
        return extract_text(data)


class BoopWyomingHandler(AsyncEventHandler):
    """Handle Wyoming describe and conversation events."""

    def __init__(
        self,
        reader: asyncio.StreamReader,
        writer: asyncio.StreamWriter,
        client: OpenCodeClient,
        registry: SessionRegistry,
    ) -> None:
        super().__init__(reader, writer)
        self.client = client
        self.registry = registry

    @staticmethod
    def info() -> Info:
        return Info(
            handle=[
                HandleProgram(
                    name="BOOP OpenCode",
                    attribution=Attribution(
                        name="BOOP / OpenCode",
                        url="https://github.com/ryankemble2006-web/boop",
                    ),
                    installed=True,
                    description="BOOP general conversation through OpenCode",
                    version="1",
                    models=[],
                    supports_handled_streaming=False,
                    supports_home_control=False,
                )
            ]
        )

    async def handle_event(self, event) -> bool:
        if Describe.is_type(event.type):
            await self.write_event(self.info().event())
            return True

        if not Transcript.is_type(event.type):
            return True

        transcript = Transcript.from_event(event)
        context = transcript.context or {}
        conversation_id = context.get("conversation_id")
        if not isinstance(conversation_id, str):
            conversation_id = None

        try:
            session_id = await self.registry.session_for(
                conversation_id, self.client.create_session
            )
            answer = await self.client.prompt(session_id, transcript.text)
            await self.write_event(Handled(text=answer, context=context).event())
        except (aiohttp.ClientError, asyncio.TimeoutError, ValueError) as err:
            _LOGGER.warning("OpenCode turn failed: %s", err)
            await self.write_event(
                NotHandled(text="BOOP's assistant did not answer", context=context).event()
            )
        except Exception:
            _LOGGER.exception("Unexpected bridge failure")
            await self.write_event(
                NotHandled(text="BOOP's assistant did not answer", context=context).event()
            )
        return True


async def run_server(host: str, port: int, opencode_url: str) -> None:
    registry = SessionRegistry()
    client = OpenCodeClient(opencode_url)
    server = AsyncTcpServer(host, port)
    _LOGGER.info("BOOP OpenCode Wyoming bridge listening on %s:%s", host, port)
    await server.run(
        handler_factory=lambda reader, writer: BoopWyomingHandler(
            reader, writer, client, registry
        )
    )


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default=DEFAULT_HOST)
    parser.add_argument("--port", type=int, default=DEFAULT_PORT)
    parser.add_argument("--opencode-url", default=DEFAULT_OPENCODE_URL)
    parser.add_argument("--debug", action="store_true")
    args = parser.parse_args()

    logging.basicConfig(
        level=logging.DEBUG if args.debug else logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s: %(message)s",
    )
    asyncio.run(run_server(args.host, args.port, args.opencode_url))


if __name__ == "__main__":
    main()
PY
chmod 700 "$BRIDGE"

# Detach from the bounded startup-hook runner's process group.
: >"$LOG"
nohup setsid "$VENV/bin/python" -u "$BRIDGE" \
    --host 0.0.0.0 --port "$WYOMING_PORT" --opencode-url "$OPENCODE" \
    >>"$LOG" 2>&1 </dev/null &
bridge_pid=$!
printf '%s\n' "$bridge_pid" >"$PID"

sleep 1
if ! kill -0 "$bridge_pid" 2>/dev/null; then
    echo "BOOP bridge failed to stay running; see $LOG" >&2
    cat "$LOG" >&2 || true
    rm -f "$PID"
    exit 1
fi

echo "BOOP OpenCode Wyoming bridge ready on internal TCP :10400 (PID $bridge_pid)"
