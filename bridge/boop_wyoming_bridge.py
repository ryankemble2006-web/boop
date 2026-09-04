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
