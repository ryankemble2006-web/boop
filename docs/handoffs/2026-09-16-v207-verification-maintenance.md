# v207 verification maintenance after installation

Date: 2026-09-16. Owner: `boop-wall-shield-split-v207`.

Installed application source remains `aa8fd9f6d79f28b441a48df31138a75d38420118`, delivered full run `35110823569`, artifact `10451993779`. The exact installed identities/hashes and subsequent Shield setup progression are recorded in `2026-09-16-v207-signed-clean-install.md`. No further installation or setup input was performed by this maintenance.

Verification-only commit `5f9dbc4e86c0ec92eba728e8b3fbdf56818c2ece` stores the 16 exact v206 packaged native-library hashes in `split/v206-native-baseline.json`. These were extracted from the hash-verified signed v206 recovery APK and had already matched both split APKs in the delivered build. Future verification no longer depends on downloading an old Actions artifact after its retention expires. The source/digest/signer/library-count checks and full per-library comparison remain enforced. No application source, resources, native runtime, Voice code or data changed.

Fresh follow-up full signed run `35114639135`, job `104856819828`: SUCCESS for all steps, including 18 inherited v206 verification steps, shell materialization and focused integration, both signed APK builds, exact APK/content/native checks and artifact upload. Focused run `35114639152`, job `104856821223`: SUCCESS; log reports 9 passed tests and 6 passing executable generated-repository/UID assertions. The production identity and setup policy harnesses remain included in that test set.

The final diff from delivered source for this maintenance contains exactly four verification-related files: the new JSON baseline, APK verifier, focused test and removal of the old artifact-download workflow step. It does not change the installed app implementation. Documentation publication separately updates the plan, current handoff/context/status/memory, main/current branch routing and exact historical archives. No independent agent review or new human acceptance is claimed.

Runtime boundaries are unchanged: Pixel 7 last observed at initial Wall setup; Shield initially captured at first setup, then later reported setup complete with YouTube open. Its later progress is not reset. The BOOP-only listener/overlay cleanup and current off state remain explicitly recorded. Broader onboarding, HA/media/sharing/notification/Johnny acceptance belongs to Ryan, and the Voice latency investigation remains deferred for Astra.
