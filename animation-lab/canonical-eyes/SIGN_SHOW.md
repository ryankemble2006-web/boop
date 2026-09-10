# Notification sign show — lab-only v8

Ryan approved the roadside-costume sign-holder concept: lift an app sign with
both five-digit hands, wobble it enthusiastically, glance toward it and blink.
This extends the existing lab, not Android notification policy or production.

Four local fixtures: WhatsApp, Gmail, Facebook and X. Signs use code-drawn arrow
props and simple letter badges, not official logo assets. No sender/message data,
notification listener, tap/dismiss authority or permissions are introduced.

The byte-locked five-digit pair (SHA-256
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1) is sampled as
two independent hand layers at runtime. The full source is unmodified. Whole
hands pivot at the wrists; no new articulated finger rig or actual finger curl
is claimed. Both hands travel with the sign's transform to keep the grip together.
The original v7 eye shader/master are unchanged and use separate state poses.

SignMotion supplies a finite eight-second performance: anticipation/lift,
side-to-side wobble, flourish, then calm hold. Choreographer's existing logical
clock supplies timing, pause/slow review, focus/lifecycle suspension and reduced
motion (show held sign). Selecting any eye clip exits the sign stage. Selecting
another sign replays the show. No automatic endless loop.

Test intent: `--ei sign 0` (0..3), optional `--ei freeze_ms 3000`, `--ez slow true`.
Run the pure Java SignMotionTest alongside MotionTest. No visual CI acceptance.
Transplant uses SignMotion, NotificationSignView and the original hands asset;
the host must explicitly integrate timing/layout and real notification semantics.

Ryan called the preceding v7 corner repair "absolutely perfect": accepted visual
preview feedback, not a physical install result or automatic approval of v8.
