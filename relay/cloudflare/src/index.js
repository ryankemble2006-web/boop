const OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses";
const MAX_TEXT_CHARS = 4000;
const UPSTREAM_TIMEOUT_MS = 28000;
const DEFAULT_MODEL = "gpt-5.6-luna";
const SPOKEN_INSTRUCTIONS = [
  "You are BOOP, an honest software puppet and home-assistant companion.",
  "Answer ordinary questions naturally and concisely for spoken playback.",
  "Do not claim sentience. Do not execute or pretend to execute home controls; those stay local.",
  "Prefer short answers unless the user clearly asks for detail."
].join(" ");

function jsonResponse(status, payload) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "content-type": "application/json; charset=utf-8" }
  });
}

function bearerToken(request) {
  const header = request.headers.get("authorization") || "";
  return header.startsWith("Bearer ") ? header.slice(7) : "";
}

function quotaLike(body) {
  const error = body && typeof body === "object" ? body.error : null;
  const code = String(error?.code || error?.type || "").toLowerCase();
  const message = String(error?.message || "").toLowerCase();
  return code.includes("quota") || message.includes("quota") || code.includes("insufficient_quota");
}

function extractOutputText(body) {
  if (!body || !Array.isArray(body.output)) return "";
  const parts = [];
  for (const item of body.output) {
    if (!item || item.type !== "message" || !Array.isArray(item.content)) continue;
    for (const content of item.content) {
      if (content?.type === "output_text" && typeof content.text === "string") {
        const text = content.text.trim();
        if (text) parts.push(text);
      }
    }
  }
  return parts.join("\n").trim();
}

async function parseJsonSafe(response) {
  try {
    return await response.json();
  } catch (_) {
    return null;
  }
}

export async function handleRequest(request, env, fetchImpl = fetch) {
  const expectedToken = String(env?.BOOP_RELAY_TOKEN || "");
  const suppliedToken = bearerToken(request);
  if (!expectedToken || !suppliedToken || suppliedToken !== expectedToken) {
    return jsonResponse(401, { ok: false, error: "auth" });
  }

  if (request.method !== "POST") {
    return jsonResponse(405, { ok: false, error: "bad_request" });
  }

  let incoming;
  try {
    incoming = await request.json();
  } catch (_) {
    return jsonResponse(400, { ok: false, error: "bad_request" });
  }

  const text = typeof incoming?.text === "string" ? incoming.text.trim() : "";
  const conversationId = typeof incoming?.conversation_id === "string"
    ? incoming.conversation_id.trim()
    : "";
  if (!text || text.length > MAX_TEXT_CHARS || conversationId.length > 256) {
    return jsonResponse(400, { ok: false, error: "bad_request" });
  }

  const apiKey = String(env?.OPENAI_API_KEY || "");
  if (!apiKey) {
    return jsonResponse(503, { ok: false, error: "service" });
  }

  const upstreamBody = {
    model: String(env?.OPENAI_MODEL || DEFAULT_MODEL),
    instructions: SPOKEN_INSTRUCTIONS,
    input: text,
    max_output_tokens: 320,
    store: true
  };
  if (conversationId) upstreamBody.previous_response_id = conversationId;

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), UPSTREAM_TIMEOUT_MS);
  let upstream;
  try {
    upstream = await fetchImpl(OPENAI_RESPONSES_URL, {
      method: "POST",
      headers: {
        "authorization": `Bearer ${apiKey}`,
        "content-type": "application/json"
      },
      body: JSON.stringify(upstreamBody),
      signal: controller.signal
    });
  } catch (error) {
    clearTimeout(timeout);
    const timedOut = error?.name === "AbortError" || error?.name === "TimeoutError";
    return jsonResponse(timedOut ? 408 : 503, {
      ok: false,
      error: timedOut ? "timeout" : "service"
    });
  }
  clearTimeout(timeout);

  const body = await parseJsonSafe(upstream);
  if (upstream.status === 401 || upstream.status === 403) {
    return jsonResponse(502, { ok: false, error: "auth" });
  }
  if (upstream.status === 408) {
    return jsonResponse(408, { ok: false, error: "timeout" });
  }
  if (upstream.status === 429) {
    return jsonResponse(429, { ok: false, error: quotaLike(body) ? "quota" : "rate_limit" });
  }
  if (upstream.status >= 500) {
    return jsonResponse(503, { ok: false, error: "service" });
  }
  if (!upstream.ok) {
    return jsonResponse(400, { ok: false, error: "bad_request" });
  }

  const answer = extractOutputText(body);
  const responseId = typeof body?.id === "string" ? body.id.trim() : "";
  if (!answer || !responseId) {
    return jsonResponse(503, { ok: false, error: "service" });
  }

  return jsonResponse(200, { ok: true, text: answer, conversation_id: responseId });
}

export default {
  async fetch(request, env) {
    return handleRequest(request, env, fetch);
  }
};
