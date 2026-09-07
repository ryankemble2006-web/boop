import test from "node:test";
import assert from "node:assert/strict";
import { handleRequest } from "../src/index.js";

const ENV = {
  OPENAI_API_KEY: "openai-test-secret",
  BOOP_RELAY_TOKEN: "relay-test-secret",
  OPENAI_MODEL: "gpt-5.6-luna"
};

function request(body, token = ENV.BOOP_RELAY_TOKEN) {
  return new Request("https://relay.example.test/", {
    method: "POST",
    headers: {
      "authorization": token ? `Bearer ${token}` : "",
      "content-type": "application/json"
    },
    body: JSON.stringify(body)
  });
}

function upstream(status, body) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" }
  });
}

async function json(response) {
  return JSON.parse(await response.text());
}

test("rejects missing or wrong relay bearer before upstream", async () => {
  let calls = 0;
  const fakeFetch = async () => { calls++; return upstream(200, {}); };
  let response = await handleRequest(request({ text: "hello" }, ""), ENV, fakeFetch);
  assert.equal(response.status, 401);
  assert.deepEqual(await json(response), { ok: false, error: "auth" });
  response = await handleRequest(request({ text: "hello" }, "wrong"), ENV, fakeFetch);
  assert.equal(response.status, 401);
  assert.equal(calls, 0);
});

test("rejects empty and oversized text", async () => {
  let calls = 0;
  const fakeFetch = async () => { calls++; return upstream(200, {}); };
  assert.equal((await handleRequest(request({ text: "   " }), ENV, fakeFetch)).status, 400);
  assert.equal((await handleRequest(request({ text: "x".repeat(4001) }), ENV, fakeFetch)).status, 400);
  assert.equal(calls, 0);
});

test("forwards only the OpenAI secret upstream and preserves conversation id", async () => {
  let seen;
  const fakeFetch = async (url, options) => {
    seen = { url, options };
    return upstream(200, {
      id: "resp_2",
      output: [{ type: "message", content: [{ type: "output_text", text: "Hello from BOOP." }] }]
    });
  };
  const response = await handleRequest(
    request({ text: "hello", conversation_id: "resp_1" }), ENV, fakeFetch);
  assert.equal(response.status, 200);
  assert.deepEqual(await json(response), {
    ok: true,
    text: "Hello from BOOP.",
    conversation_id: "resp_2"
  });
  assert.equal(seen.url, "https://api.openai.com/v1/responses");
  assert.equal(seen.options.headers.authorization, `Bearer ${ENV.OPENAI_API_KEY}`);
  assert.ok(!JSON.stringify(seen.options).includes(ENV.BOOP_RELAY_TOKEN));
  const body = JSON.parse(seen.options.body);
  assert.equal(body.previous_response_id, "resp_1");
  assert.equal(body.input, "hello");
  assert.match(body.instructions, /home controls.*local/i);
});

test("maps upstream auth without returning raw upstream body", async () => {
  const fakeFetch = async () => upstream(401, {
    error: { message: `bad key ${ENV.OPENAI_API_KEY}` }
  });
  const response = await handleRequest(request({ text: "hello" }), ENV, fakeFetch);
  const body = await json(response);
  assert.equal(body.error, "auth");
  assert.ok(!JSON.stringify(body).includes(ENV.OPENAI_API_KEY));
});

test("maps quota and normal rate limit separately", async () => {
  let response = await handleRequest(request({ text: "hello" }), ENV,
    async () => upstream(429, { error: { code: "insufficient_quota", message: "quota exceeded" } }));
  assert.deepEqual(await json(response), { ok: false, error: "quota" });

  response = await handleRequest(request({ text: "hello" }), ENV,
    async () => upstream(429, { error: { code: "rate_limit_exceeded" } }));
  assert.deepEqual(await json(response), { ok: false, error: "rate_limit" });
});

test("maps timeout, network and 5xx without leaking secrets", async () => {
  let response = await handleRequest(request({ text: "hello" }), ENV, async () => {
    const error = new Error("timed out");
    error.name = "AbortError";
    throw error;
  });
  assert.equal((await json(response)).error, "timeout");

  response = await handleRequest(request({ text: "hello" }), ENV, async () => {
    throw new Error(`network failed ${ENV.OPENAI_API_KEY} ${ENV.BOOP_RELAY_TOKEN}`);
  });
  let body = await json(response);
  assert.equal(body.error, "service");
  assert.ok(!JSON.stringify(body).includes(ENV.OPENAI_API_KEY));
  assert.ok(!JSON.stringify(body).includes(ENV.BOOP_RELAY_TOKEN));

  response = await handleRequest(request({ text: "hello" }), ENV,
    async () => upstream(503, { secret: ENV.OPENAI_API_KEY }));
  body = await json(response);
  assert.deepEqual(body, { ok: false, error: "service" });
});

test("rejects malformed success instead of exposing upstream structure", async () => {
  const response = await handleRequest(request({ text: "hello" }), ENV,
    async () => upstream(200, { id: "resp_1", output: [] }));
  assert.deepEqual(await json(response), { ok: false, error: "service" });
});
