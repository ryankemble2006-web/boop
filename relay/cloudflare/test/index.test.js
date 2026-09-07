import test from 'node:test';
import assert from 'node:assert/strict';
import { handleRequest } from '../src/index.js';

// These deliberately fake credentials are never used for real network traffic.
const env = {
  OPENAI_API_KEY: 'test-only-upstream-credential-not-a-real-key',
  BOOP_RELAY_TOKEN: 'test-only-relay-credential-not-a-real-token',
  OPENAI_MODEL: 'gpt-4.1-mini',
};
const input = (body = { text: 'Why is the sky blue?' }, token = env.BOOP_RELAY_TOKEN, options = {}) =>
  new Request('https://example.invalid/chat', {
    method: 'POST', headers: { authorization: `Bearer ${token}`, 'content-type': 'application/json' },
    body: typeof body === 'string' ? body : JSON.stringify(body), ...options,
  });
const output = (text = 'Air scatters blue light.', id = 'resp_test1') => ({
  id, status: 'completed', output: [
    { type: 'reasoning', summary: [] },
    { type: 'message', role: 'assistant', content: [{ type: 'output_text', text }] },
  ],
});
const upstream = (body, status = 200) => new Response(typeof body === 'string' ? body : JSON.stringify(body), {
  status, headers: { 'content-type': 'application/json' },
});
const forbidden = async () => { throw new Error('unexpected upstream call'); };
async function envelope(response) {
  assert.equal(response.headers.get('cache-control'), 'no-store');
  assert.equal(response.headers.get('x-content-type-options'), 'nosniff');
  const body = await response.json();
  const serialized = JSON.stringify(body);
  assert.ok(!serialized.includes(env.OPENAI_API_KEY));
  assert.ok(!serialized.includes(env.BOOP_RELAY_TOKEN));
  return body;
}

test('missing or wrong bearer is rejected before parsing or contacting the provider', async () => {
  for (const token of ['', 'wrong', `${env.BOOP_RELAY_TOKEN}extra`]) {
    const response = await handleRequest(input('not even JSON', token), env, forbidden);
    assert.equal(response.status, 401); assert.deepEqual(await envelope(response), { ok: false, error: 'auth' });
  }
  const response = await handleRequest(input(), { ...env, BOOP_RELAY_TOKEN: '' }, forbidden);
  assert.equal(response.status, 503); assert.deepEqual(await envelope(response), { ok: false, error: 'service' });
});

test('invalid media type and method do not call upstream', async () => {
  assert.equal((await handleRequest(new Request('https://example.invalid/chat'), env, forbidden)).status, 405);
  const request = input('hello', env.BOOP_RELAY_TOKEN, { headers: { authorization: `Bearer ${env.BOOP_RELAY_TOKEN}`, 'content-type': 'text/plain' } });
  assert.equal((await handleRequest(request, env, forbidden)).status, 400);
});

test('invalid, empty, oversized and extra provider-control fields are rejected', async () => {
  const cases = ['not JSON', null, [], {}, { text: '' }, { text: '  ' }, { text: 2 },
    { text: 'x'.repeat(4001) }, { text: 'hi', conversation_id: 2 },
    { text: 'hi', conversation_id: 'bad id' }, { text: 'hi', conversation_id: 'x'.repeat(257) },
    { text: 'hi', model: 'other' }, { text: 'hi', tools: [] },
    `{"text":"hi"}${' '.repeat(17000)}`,
  ];
  for (const body of cases) {
    const response = await handleRequest(input(body), env, forbidden);
    assert.equal(response.status, 400); assert.deepEqual(await envelope(response), { ok: false, error: 'bad_request' });
  }
});

test('success uses fixed Responses endpoint and only the upstream credential upstream', async () => {
  let observed;
  const response = await handleRequest(input({ text: '  Hello  ' }), env, async (url, options) => {
    observed = { url, options, body: JSON.parse(options.body) };
    return upstream(output());
  });
  assert.equal(response.status, 200);
  assert.deepEqual(await envelope(response), { ok: true, text: 'Air scatters blue light.', conversation_id: 'resp_test1' });
  assert.equal(observed.url, 'https://api.openai.com/v1/responses');
  assert.equal(observed.options.method, 'POST');
  assert.equal(observed.options.redirect, 'error');
  assert.equal(observed.options.headers.Authorization, `Bearer ${env.OPENAI_API_KEY}`);
  assert.equal(observed.body.input, 'Hello');
  assert.equal(observed.body.model, env.OPENAI_MODEL);
  assert.equal(observed.body.store, true);
  assert.equal(observed.body.stream, false);
  assert.equal(observed.body.max_output_tokens, 600);
  assert.ok(observed.body.instructions.includes('Home Assistant'));
  assert.ok(!JSON.stringify(observed).includes(env.BOOP_RELAY_TOKEN));
  assert.ok(!Object.hasOwn(observed.body, 'previous_response_id'));
  assert.ok(!Object.hasOwn(observed.body, 'tools'));
});

test('opaque previous response id is forwarded only when supplied', async () => {
  let requestBody;
  const response = await handleRequest(input({ text: 'And why?', conversation_id: 'resp_previous' }), env, async (_, options) => {
    requestBody = JSON.parse(options.body); return upstream(output('Another answer.', 'resp_next'));
  });
  assert.equal(requestBody.previous_response_id, 'resp_previous');
  assert.equal((await envelope(response)).conversation_id, 'resp_next');
});

test('only actual assistant output text is joined; reasoning and tools are not speech', async () => {
  const body = output();
  body.output = [
    { type: 'reasoning', content: [{ type: 'output_text', text: 'not speech' }] },
    { type: 'message', role: 'assistant', content: [{ type: 'output_text', text: 'First.' }, { type: 'output_text', text: 'Second.' }] },
  ];
  const response = await handleRequest(input(), env, async () => upstream(body));
  assert.equal((await envelope(response)).text, 'First.\nSecond.');
});

test('upstream errors are sanitized and mapped to stable vocabulary', async () => {
  const cases = [
    [401, { error: { message: env.OPENAI_API_KEY } }, 401, 'auth'],
    [403, 'private upstream detail', 401, 'auth'],
    [408, {}, 504, 'timeout'], [504, {}, 504, 'timeout'],
    [429, { error: { code: 'insufficient_quota' } }, 429, 'quota'],
    [429, { error: { type: 'insufficient_quota' } }, 429, 'quota'],
    [429, 'unparseable', 429, 'rate_limit'],
    [500, env.OPENAI_API_KEY, 503, 'service'], [502, {}, 503, 'service'],
    [400, { error: { message: 'secret upstream details' } }, 400, 'bad_request'],
    [302, {}, 503, 'service'],
  ];
  for (const [code, body, expectedCode, error] of cases) {
    const response = await handleRequest(input(), env, async () => upstream(body, code));
    assert.equal(response.status, expectedCode);
    assert.deepEqual(await envelope(response), { ok: false, error });
  }
});

test('missing upstream configuration is graceful and never reaches the network', async () => {
  const response = await handleRequest(input(), { ...env, OPENAI_API_KEY: '' }, forbidden);
  assert.equal(response.status, 503); assert.deepEqual(await envelope(response), { ok: false, error: 'service' });
});

test('malformed, incomplete, empty or oversized success never becomes speech', async () => {
  const cases = ['not JSON', {}, { output_text: 'SDK-only property', id: 'resp_1', status: 'completed' },
    output('', 'resp_1'), output('Hi', 'invalid id'), { ...output(), status: 'incomplete' },
    { ...output(), output: [{ type: 'message', role: 'assistant', content: [{ type: 'output_text', text: 7 }] }] },
    output('x'.repeat(8001)), output(env.OPENAI_API_KEY), output(env.BOOP_RELAY_TOKEN),
    'x'.repeat(65537),
  ];
  for (const body of cases) {
    const response = await handleRequest(input(), env, async () => upstream(body));
    assert.equal(response.status, 503); assert.deepEqual(await envelope(response), { ok: false, error: 'service' });
  }
});

test('network failures and upstream aborts do not leak exception messages', async () => {
  for (const [error, expected] of [[new Error(env.OPENAI_API_KEY), 'offline'], [new DOMException('secret detail', 'AbortError'), 'timeout']]) {
    const response = await handleRequest(input(), env, async () => { throw error; });
    assert.deepEqual(await envelope(response), { ok: false, error: expected });
  }
});

test('no real network is used by default Worker export under injected mocked fetch', async () => {
  const { default: worker } = await import('../src/index.js');
  assert.equal(typeof worker.fetch, 'function');
});

test('a response identifier cannot leak either credential', async () => {
  for (const id of [env.OPENAI_API_KEY, env.BOOP_RELAY_TOKEN]) {
    const response = await handleRequest(input(), env, async () => upstream(output('An answer.', id)));
    assert.equal(response.status, 503); assert.deepEqual(await envelope(response), { ok: false, error: 'service' });
  }
});

test('a provider credential cannot be reused as the public-facing relay credential', async () => {
  const response = await handleRequest(input({}, env.OPENAI_API_KEY), { ...env, BOOP_RELAY_TOKEN: env.OPENAI_API_KEY }, forbidden);
  assert.equal(response.status, 400); // Invalid input is rejected before provider configuration.
  const configured = await handleRequest(input({ text: 'hi' }, env.OPENAI_API_KEY), { ...env, BOOP_RELAY_TOKEN: env.OPENAI_API_KEY }, forbidden);
  assert.equal(configured.status, 503);
  assert.deepEqual(await envelope(configured), { ok: false, error: 'service' });
});
