# Unified v163 music permission integration: pre-build checkpoint

Updated 2026-09-13. Ryan reported Music Lab would not load Now Playing and explicitly requested rolling this work into Unified v162 and bumping to163. This supersedes the earlier no-merge boundary for this feature only.

LIVE accepted base: boop-unified-eye-sync-safe-v159 at112d09b5b446d6582954a6d89b3700fe16298ecb. Candidate branch boop-unified-music-v163. Reviewed permission donation from boop-music-lab-side-by-side-v161 at7b596a3be376b30f06676997d70b7b50bf8494c0. Implemented source cac499bf9e6ab325faa2d843112f90ace71118b1.

Only five app-input paths change: two permission classes (exact existing blobs), six settings lines, one private activity in the v162 manifest and version163 /1.2.163-music-audio-access. Package com.boop.alpha1/signing remain unchanged. All other app inputs, including native lyrics, media/session observation, voice, approved artwork/animation and build scripts, retain accepted v162 bytes. No whole-lab merge, package migration or role removals.

Read-only Shield check found Unified's media-listener access true and Music Lab's false; Unified version162. No notification content, credentials, media transport input, app launch or permission change. This finding explains a missing requirement for the lab; no exhaustive runtime diagnosis is claimed.

RED commit7cb39385aa4f5f20502795c8c4d68e438e37c61e, run34776776511/job103776215998: eight checks discovered, five failed for missing permission integration/version, three release163-specific checks skipped. Failure logs read before production transplant. GREEN run34776875654/job103776490381 atcac499bf: workflow success. It runs the unchanged 18-decision Java permission harness and source/manifest/transplant checks. Comparison to live accepted base confirms no unrelated app path changed. Source/lifecycle/diff review was in-session, not independent.

The live owner was reread at112d09b5 immediately before fast-forwarding, without force, to reviewed sourcecac499bf. Its normal signed Unified workflow should now run; full build, APK and v163 installation are not yet claimed in this checkpoint. Source code was not edited or built on the laptop. These handoff/status/memory revisions are documentation only.

Preserve Ryan's v162 native lyrics/Skip acceptance and v161 speed/two-way-colour acceptance. Latest accepted v162 APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171; permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde. Full earlier notes at112d09b5 and dated receipts remain recoverable.

Next: finish full GitHub checks/signing, verify artifact/version/signature/hash and update Shield in place only. No reset, grant, phone/emulator action or lab uninstall. Runtime acceptance remains joint with Ryan. Actual Visualizer sampling/VU bounce remains UNIMPLEMENTED; this is the existing conditional permission feature only.
