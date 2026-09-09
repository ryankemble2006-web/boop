# BOOP v85 Natural Voice + Tablet Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Produce one signed `com.boop.alpha1` v85 candidate containing the already-green Android tablet routing plus natural offline voices, while fixing the user-observed post-download `Verifying natural voices…` stall.

**Architecture:** Start from live canonical `boop-unified@3527abf5804b9bfa2911f3a6e1abb84ed2ed8582`, which already contains the 600dp tablet -> Wall routing and current locked glossy-eye state. Integrate only the natural-voice delta from `boop-v70-natural-voices@2442270efc29cc291bc399853162ca119e2c0283`, then change the download/install pipeline so SHA-256 is accumulated while bytes are downloaded, verification becomes an immediate size/hash comparison, extraction is reported separately as installation progress, and cancellation does not synchronously block the UI on pack cleanup.

**Tech Stack:** Android Java 17, OkHttp 4.12, Sherpa-ONNX 1.13.7, Apache Commons Compress 1.28.0, JUnit 4, pytest contract gates, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-09-natural-voices-v70-design.md` plus the current tablet-routing contract in `BOOP_UNIFIED_MEMORY.md`.

## Global Constraints

- Keep package `com.boop.alpha1` and the permanent BOOP signer unchanged.
- Set `versionCode 85`; use a v85 version name that identifies the combined natural-voice/tablet candidate.
- Preserve routing: override first; TV/Leanback -> Shield; Pixel 7 Pro -> Wall; other non-TV `smallestScreenWidthDp >= 600` -> Wall; sub-600dp -> Launcher.
- Preserve exact approved glossy eye master and all existing visual/manual-acceptance rules. No generated or reinterpreted artwork.
- Keep the existing four natural voices and IDs unchanged: Emma 21, Isabella 22, George 26, Fable 25.
- Natural pack remains optional, app-private, hash-verified, local/offline after install, with Android TTS fallback.
- Latest fully physically accepted rollback remains v59; do not repoint protected checkpoints.

---

### Task 1: Integrate the already-green natural voice delta onto the current tablet-capable Unified base

**Files:**
- Add natural voice source/tests/manifest/patch files from `boop-v70-natural-voices@2442270e`.
- Modify `scripts/materialize-unified.sh` to run the natural voice patch after v70 settings repair and before notification/dev-menu patches.
- Modify `source/app-build.gradle` and `unified/app-build.gradle` to retain Commons Compress.
- Modify `.github/workflows/build-boop-unified.yml` to run natural voice gates on `boop-v85-natural-tablet` while retaining canonical Unified gates.

**Interfaces:**
- Consumes: current `boop-unified` tablet routing and eye state.
- Produces: a combined branch that materializes the same natural voice feature without changing `BoopDeviceProfile` tablet semantics.

- [ ] **Step 1:** Copy the natural voice delta only, preserving target-branch tablet/eye files.
- [ ] **Step 2:** Compare the combined branch against both source lineages and verify no tablet routing or locked-eye asset is removed.
- [ ] **Step 3:** Commit integration before bug-fix production changes.

### Task 2: Reproduce the verify-stall contract first

**Files:**
- Create: `tests/test_unified_v85_natural_voice_install_flow.py`
- Existing production under test: `source/BoopNaturalVoiceDownloader.java`, `source/BoopNaturalVoicePack.java`, `scripts/patch-unified-natural-voices.py`

**Required failing contract before production fix:**

```python
from pathlib import Path


def test_v85_natural_voice_install_flow_is_stream_verified_and_reports_install_phase():
    downloader = Path('source/BoopNaturalVoiceDownloader.java').read_text()
    patch = Path('scripts/patch-unified-natural-voices.py').read_text()
    assert 'MessageDigest' in downloader
    assert 'digest.update(buffer, 0, count)' in downloader
    assert 'Installing natural voices' in downloader
    assert 'onInstallProgress' in downloader
    assert 'onInstallProgress' in patch
```

- [ ] **Step 1:** Add the test above before changing production.
- [ ] **Step 2:** Run it in CI and record RED because the current downloader performs a second full-file SHA pass and exposes extraction under the single `Verifying natural voices…` status.
- [ ] **Step 3:** Do not weaken the test to make RED disappear.

### Task 3: Fix post-download verification/install responsiveness

**Files:**
- Modify: `source/BoopNaturalVoiceDownloader.java`
- Modify: `source/BoopNaturalVoicePack.java`
- Modify: `scripts/patch-unified-natural-voices.py`
- Test: `tests/test_unified_v85_natural_voice_install_flow.py`

**Interfaces:**
- Downloader computes SHA-256 over the exact bytes written and retains downloaded-size verification.
- Pack accepts the observed digest from the completed download for the production install path, while keeping a self-hashing overload for focused pack tests/recovery use.
- Extraction reports throttled percentage using compressed bytes consumed.
- Cancel sets the cancellation flag and lets the worker terminate/clean up rather than synchronously calling pack cleanup from the UI thread.

- [ ] **Step 1:** Add a digest to the download write loop and compare size/hash immediately after download.
- [ ] **Step 2:** Emit `Verifying natural voices…` only for the immediate receipt comparison, then `Installing natural voices…` for extraction.
- [ ] **Step 3:** Add install-progress callback plumbing and show `Installing natural voices… N%` in Voice Settings.
- [ ] **Step 4:** Make cancellation cooperative and non-blocking; hash/extract loops must observe cancellation promptly.
- [ ] **Step 5:** Run the focused v85 install-flow contract and existing natural voice unit/contracts to GREEN.

### Task 4: Version the combined candidate as v85

**Files:**
- Modify: `unified/app-build.gradle`
- Test: workflow `Read expected unified version` plus signed APK badging receipt.

- [ ] **Step 1:** Set `versionCode 85` and a v85 combined version name.
- [ ] **Step 2:** Materialize and confirm the generated app uses the same code/name.
- [ ] **Step 3:** Confirm package and signer are unchanged.

### Task 5: Full combined verification and canonical merge

**Files:**
- Update after green: `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`.

- [ ] **Step 1:** Run the full Unified workflow on `boop-v85-natural-tablet`, including tablet profile tests, natural voice tests, Shield tests, wake/routing/lifecycle tests, package/signature/integrity checks and signed APK upload.
- [ ] **Step 2:** Download the exact artifact and independently verify artifact digest, APK hash, `built-commit.txt`, package/version badging and permanent signer receipt.
- [ ] **Step 3:** Update handoff/status/memory with exact app head, workflow, artifact, hashes and the physical acceptance boundary.
- [ ] **Step 4:** Open a PR from `boop-v85-natural-tablet` to `boop-unified`; merge only if the feature branch is green and the live `boop-unified` head has not moved incompatibly.
- [ ] **Step 5:** Verify live `boop-unified` and `main` heads after merge. Do not update `main` unless the shared product/branch contract changed.
