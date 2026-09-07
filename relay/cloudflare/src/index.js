/** Single-household prototype relay. No OpenAI credential is sent to Android. */
const OPENAI_URL = 'https://api.openai.com/v1/responses';
const MAX_REQUEST_BYTES = 16_384;
const MAX_RESPONSE_BYTES = 65_536;
const MAX_TEXT = 4_000;
const MAX_REPLY = 8_000;
const UPSTREAM_TIMEOUT_MS = 25_000; // Leave time for Android's 30-second call deadline.
const ID = /^[A-Za-z0-9_-]{1,256}$/;
const INSTRUCTIONS = 'You are BOOP, an expressive software puppet, not a person. '
  + 'Give concise, friendly answers intended to be spoken aloud. Be honest about uncertainty. '
  + 'Do not claim sentience. Home Assistant handles all home and media controls locally. '
  + 'You have no device-control tools; never claim that you executed a command or changed a device.';

function json(status, body) {
  return new Response(JSON.stringify(body), { status, headers: {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    'X-Content-Type-Options': 'nosniff',
  } });
}
const failure = (status, error) => json(status, { ok: false, error });

/** Compare fixed-size digests so common prefixes do not determine comparison time. */
async function tokenMatches(actual, expected) {
  if (!actual || actual.length > 4096) return false;
  const encoder = new TextEncoder();
  const [a, b] = await Promise.all([actual, expected].map(value =>
    crypto.subtle.digest('SHA-256', encoder.encode(value))));
  const av = new Uint8Array(a), bv = new Uint8Array(b);
  let difference = 0;
  for (let i = 0; i < av.length; i++) difference |= av[i] ^ bv[i];
  return difference === 0;
}

/** Bounds decoded bodies even when Content-Length is absent or incorrect. */
async function boundedJson(message, limit) {
  const length = message.headers.get('content-length');
  if (length !== null && (!/^\d+$/.test(length) || Number(length) > limit)) {
    if (message.body) await message.body.cancel().catch(() => {});
    throw new Error('body limit');
  }
  if (!message.body) throw new Error('empty body');
  const reader = message.body.getReader();
  const decoder = new TextDecoder('utf-8', { fatal: true });
  let bytes = 0, text = '';
  try {
    for (;;) {
      const { done, value } = await reader.read();
      if (done) break;
      bytes += value.byteLength;
      if (bytes > limit) throw new Error('body limit');
      text += decoder.decode(value, { stream: true });
    }
    text += decoder.decode();
    return JSON.parse(text);
  } catch (error) {
    await reader.cancel().catch(() => {});
    throw error;
  } finally {
    reader.releaseLock();
  }
}

function validInput(body) {
  return body !== null && typeof body === 'object' && !Array.isArray(body)
    && Object.keys(body).every(key => key === 'text' || key === 'conversation_id')
    && typeof body.text === 'string' && body.text.trim().length > 0 && body.text.length <= MAX_TEXT
    && (body.conversation_id === undefined || (typeof body.conversation_id === 'string'
      && ID.test(body.conversation_id)));
}

function spokenAnswer(body) {
  if (!body || body.status !== 'completed' || typeof body.id !== 'string'
      || !ID.test(body.id) || !Array.isArray(body.output)) return null;
  const parts = [];
  for (const item of body.output) {
    if (item?.type !== 'message' || item.role !== 'assistant' || !Array.isArray(item.content)) continue;
    for (const part of item.content) {
      if (part?.type !== 'output_text') continue;
      if (typeof part.text !== 'string') return null;
      parts.push(part.text);
    }
  }
  const text = parts.join('\n').trim();
  return text && text.length <= MAX_REPLY ? text : null;
}

/** fetchUpstream is the standard fetch signature, injected only at the transport boundary. */
export async function handleRequest(request, env, fetchUpstream = fetch) {
  if (request.method !== 'POST') return failure(405, 'bad_request');
  const token = typeof env.BOOP_RELAY_TOKEN === 'string' ? env.BOOP_RELAY_TOKEN : '';
  if (!token || token.length > 4096) return failure(503, 'service');
  const authorization = request.headers.get('authorization') || '';
  if (!authorization.startsWith('Bearer ')
      || !await tokenMatches(authorization.slice(7), token)) return failure(401, 'auth');
  // Authenticate before examining the utterance or contacting any provider.
  if ((request.headers.get('content-type') || '').split(';')[0].trim().toLowerCase()
      !== 'application/json') return failure(400, 'bad_request');
  let input;
  try { input = await boundedJson(request, MAX_REQUEST_BYTES); }
  catch { return failure(400, 'bad_request'); }
  if (!validInput(input)) return failure(400, 'bad_request');
  const key = typeof env.OPENAI_API_KEY === 'string' ? env.OPENAI_API_KEY.trim() : '';
  if (!key || key === token) return failure(503, 'service');
  const model = env.OPENAI_MODEL || 'gpt-4.1-mini';
  if (typeof model !== 'string' || !/^[A-Za-z0-9._:-]{1,128}$/.test(model)) return failure(503, 'service');
  const body = {
    model, instructions: INSTRUCTIONS, input: input.text.trim(),
    max_output_tokens: 600, stream: false, store: true,
  };
  if (input.conversation_id !== undefined) body.previous_response_id = input.conversation_id;
  const controller = new AbortController();
  const timer = setTimeout(() => controller.abort(), UPSTREAM_TIMEOUT_MS);
  try {
    const response = await fetchUpstream(OPENAI_URL, {
      method: 'POST', redirect: 'error', signal: controller.signal,
      headers: { Authorization: `Bearer ${key}`, 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(body),
    });
    if (response.status === 401 || response.status === 403) {
      await response.body?.cancel().catch(() => {});
      return failure(401, 'auth');
    }
    if (response.status === 408 || response.status === 504) {
      await response.body?.cancel().catch(() => {});
      return failure(504, 'timeout');
    }
    let parsed;
    try { parsed = await boundedJson(response, MAX_RESPONSE_BYTES); }
    catch (error) {
      if (controller.signal.aborted || error?.name === 'AbortError') return failure(504, 'timeout');
      if (response.status === 429) return failure(429, 'rate_limit');
      return failure(response.status === 400 ? 400 : 503, response.status === 400 ? 'bad_request' : 'service');
    }
    if (response.status === 429) {
      const quota = parsed?.error?.code === 'insufficient_quota' || parsed?.error?.type === 'insufficient_quota';
      return failure(429, quota ? 'quota' : 'rate_limit');
    }
    if (response.status === 400 || response.status === 404) return failure(400, 'bad_request');
    if (response.status !== 200) return failure(503, 'service');
    const text = spokenAnswer(parsed);
    if (!text || text.includes(key) || text.includes(token)
        || parsed.id.includes(key) || parsed.id.includes(token)) return failure(503, 'service');
    return json(200, { ok: true, text, conversation_id: parsed.id });
  } catch (error) {
    return controller.signal.aborted || error?.name === 'AbortError'
      ? failure(504, 'timeout') : failure(503, 'offline');
  } finally {
    clearTimeout(timer);
  }
}

export default { fetch: (request, env) => handleRequest(request, env) };
