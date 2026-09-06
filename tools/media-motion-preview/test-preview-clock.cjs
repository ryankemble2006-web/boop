// Exercise the actual preview script with a controlled animation-frame clock.
// Removing its 1.2 speed multiplier must fail the elapsed-time assertion.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

(async () => {
  const listeners = {};
  const button = {setAttribute() {}, addEventListener(name, callback) {listeners[name] = callback;}};
  const root = {
    dataset: {}, isConnected: true,
    querySelectorAll() {return [];}, // Artwork was visually checked separately.
    querySelector(selector) {return selector.includes('pause') ? button : {};}
  };
  const document = {hidden: false, getElementById() {return root;}, addEventListener() {}};
  let nextFrame = null;
  const context = vm.createContext({
    document,
    matchMedia: () => ({matches: false, addEventListener() {}}),
    requestAnimationFrame(callback) {nextFrame = callback; return 1;},
    cancelAnimationFrame() {nextFrame = null;}
  });
  const motion = fs.readFileSync(path.join(__dirname, 'motion.json'), 'utf8').replace(/^\uFEFF/, '');
  const template = fs.readFileSync(path.join(__dirname, 'preview.fragment.html'), 'utf8');
  const script = template.match(/<script>([\s\S]*?)<\/script>/)[1]
    .replace('__MOTION__', motion).replace('__SOURCES__', '{}');
  vm.runInContext(script, context);
  await new Promise(setImmediate); // Resolve the script's image-ready Promise.
  function frame(time) {assert.equal(typeof nextFrame, 'function'); nextFrame(time);}
  frame(0);
  for (let now = 100; now <= 1000; now += 100) frame(now);
  assert.equal(root.boopPreview.elapsed, 1200, 'one real second advances 1.2 seconds of motion');
  for (let now = 1100; now <= 10000; now += 100) frame(now);
  assert.equal(root.boopPreview.elapsed, 12000, 'popcorn completes its loop in ten real seconds');
  assert.deepEqual(root.boopPreview.sample('cinema', root.boopPreview.elapsed),
    root.boopPreview.sample('cinema', 0), 'faster loop preserves its original pose sequence');
  listeners.click();
  assert.equal(nextFrame, null, 'manual pause cancels animation');
  const pausedAt = root.boopPreview.elapsed;
  listeners.click();
  frame(50000);
  assert.equal(root.boopPreview.elapsed, pausedAt, 'resume does not jump through paused time');
  frame(50100);
  assert.equal(root.boopPreview.elapsed, pausedAt + 120, 'resumed playback retains the new speed');
  document.hidden = true;
  frame(50200);
  assert.equal(root.boopPreview.elapsed, pausedAt + 120, 'hidden page does not advance');
  console.log('PASS: 1.2x playback, ten-second popcorn loop, pause/resume and hidden-page timing');
})().catch(error => {console.error(error); process.exitCode = 1;});
