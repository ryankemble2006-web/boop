# Notification sign show — lab-only v9 grip candidate

Ryan approved the roadside-costume sign-holder concept: lift an app sign with
both five-digit hands, wobble it enthusiastically, glance toward it and blink.
This extends the existing lab, not Android notification policy or production.

Four local fixtures: WhatsApp, Gmail, Facebook and X. Signs use code-drawn arrow
props and simple letter badges, not official logo assets. No sender/message data,
notification listener, tap/dismiss authority or permissions are introduced.

The byte-locked five-digit pair (SHA-256
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1) is sampled as
two palms/thumbs at rear depth and eight independently sampled finger strips at
front depth. The full source is unmodified. Each strip is foreshortened through
a small Canvas mesh, four fingers per hand facing the viewer. Palm/thumb material
is behind the sign. This is a restrained 2D grip, not newly generated artwork or
a full 3D hand. All layers travel with the sign's transform to keep contact.
Ryan rejected v8's flat open hands drawn on top of the board. That screenshot is
the manual visual failure reproduction; v9 needs Ryan's appearance review.
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
