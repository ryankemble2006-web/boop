# Natural voice settings demos

These eight small PCM clips contain the existing named-voice introductions and
"This is how BOOP sounds." in Emma, Isabella, George and Fable. They remove model
loading and neural inference from the settings buttons, including the first tap.
They do not contain recordings from the user's microphone.

Generated locally with the existing verified `kokoro-multi-lang-v1_0` pack and
Sherpa-ONNX 1.13.7, through the installed Wall v210 backend at source
`a026b967e69429b4c95fd698b571297c0e6f9ba3`. `receipt.json` pins the source archive,
speaker IDs, text, generation parameters, sample counts and clip hashes.

Reproduction uses the pinned pack from `../manifest.json` with its British
lexicon and espeak data. Construct the existing `OfflineTtsKokoroModelConfig`,
use CPU/four threads, and call `generateWithConfig` for each receipt text with
its speaker ID, speed `1.0`, and silence scale `0.2`. Convert each generated float
sample using `round(clamp(sample, -1, 1) * 32767)` and write signed 16-bit little
endian mono PCM at 24 kHz. No JNI streaming callback is used. Float inference
may differ slightly across hardware; intentional regeneration updates hashes.

The model's Apache 2.0 license is retained in `MODEL-LICENSE.txt`. The exact
[official model archive](https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-multi-lang-v1_0.tar.bz2)
is already the app's optional voice download.

Both fixed demos and freshly generated replies now apply saved pitch and cadence
to neutral PCM with Android PlaybackParams. No slider ranges or saved values are
changed. A missing/corrupt demo falls back to local generation; a playback tuning
failure is reported instead of silently pretending an untuned sample matches.
