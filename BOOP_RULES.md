# BOOP Rules

BOOP is a puppet interface, not a chatbot.

Never:
- add cloud dependency for basic control
- add microphone to overlay
- make screen touch required
- replace existing working functions without approval

Always:
- local Home Assistant first
- remote friendly
- big chunky UI
- plain English errors
- test before claiming done

## APK delivery to Ryan's private FTP, approved 2026-09-08

For BOOP APK handoffs, the default delivery path is Ryan's private FTP `apk/` folder.
After the exact signed APK has passed the applicable functional/build/package/signer
checks, upload that same APK to the private FTP destination whenever the active
session has authorized FTP upload capability and runtime credentials. Use the
versioned APK filename unless Ryan explicitly asks to overwrite a stable name.

FTP is a delivery convenience, not the source-of-truth archive. Keep the exact Git
source commit, workflow/artifact receipt, APK hash and signer provenance as the
canonical build evidence.

Never commit or publish the FTP host, port, username, password, encoded password,
client XML/export, or other connection secrets in this public repository, docs,
source, workflow files, issues, logs or artifacts. FTP credentials are private
runtime configuration only.

Verify transfer completion and report the remote `apk/` path/filename when the
current tool can confirm it. If FTP upload is unavailable or fails, do not claim it
succeeded: hand over the exact verified APK through the available artifact path and
state that FTP delivery is still pending. Do not upload unrelated files unless Ryan
explicitly asks.

## Manual visual acceptance, approved 2026-09-08

Ryan owns BOOP visual acceptance on his real devices. Do not add or run GitHub visual checks, screenshot/golden-image comparisons, appearance/layout/animation judging, or source-string guards that try to certify how BOOP looks. Do not silently re-enable them in later work. Only Ryan's explicit reversal changes this rule.

Keep non-visual compilation, focused functional tests, package integrity, permanent-signer verification and security checks. A process/entry-activity crash smoke test is non-visual; it must not be presented as proof that the UI looks right. Upload the verified signed test APK before an optional slower non-visual emulator smoke so visual inspection is not delayed.

Approved artwork remains locked. Animate or mask the existing art in code; do not regenerate it per pose. User-selected eye hue changes only the blue/cyan iris region, not the sclera, pupil, highlights or the rest of the eyeball. Physical appearance and acoustic wake accuracy remain manual acceptance requirements.
