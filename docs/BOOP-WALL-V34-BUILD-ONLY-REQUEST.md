# BOOP Wall v34 build-only request

Updated 2026-09-07.
Owning branch: `boop-wall-free-chat-wip`.

Ryan explicitly requested: leave testing to him from now and deliver the APK.
This build request must use the existing `[boop-build-only]` workflow path: no
source/JVM/Worker/emulator/runtime tests, no production-code changes, and no new
signing material. The workflow must still materialize the current Wall source,
build versionCode 34 / `0.4.14-wall-native-chat`, verify package/version/archive
and signer continuity against the existing permanent BOOP certificate, and upload
the resulting installable APK plus public fingerprints.

Application source state before this request: `72b9ae7d969e63f2b1223d82c0792177a67583fa`.
The previous full run built and signed v34 successfully through package/version/
archive/signer verification; artifact upload was skipped only because a later
polish-emulator gate failed. That failure is not being retried at the user's
explicit request.

Do not repoint accepted physical checkpoints. Physical/runtime acceptance of v34
belongs to Ryan's device testing after delivery.
