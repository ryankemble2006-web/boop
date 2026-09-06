# BOOP Shield emulator test lab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create and verify a host-NVIDIA-GPU-accelerated Android TV emulator for rapid BOOP Shield debug testing while retaining the physical NVIDIA SHIELD as the release gate.

**Architecture:** A stock Android TV AVD uses Android Emulator hardware graphics. Windows routes its process to the discrete NVIDIA GPU where supported and renderer output verifies acceleration. The AVD is a development target; the physical Shield remains the acceptance target.

**Tech Stack:** Windows graphics preferences, NVIDIA driver, Android SDK Emulator, Android TV system image, AVD Manager, ADB, BOOP Android debug APK.

**Spec:** `docs/superpowers/specs/2026-09-06-shield-emulator-test-lab-design.md`

## Global Constraints

- Use a standard Android TV AVD at 1920x1080 with hardware graphics rendering on the host NVIDIA GPU.
- Do not claim GPU passthrough or emulate NVIDIA SHIELD firmware.
- Do not install software, change global GPU policy, or alter virtualization/security settings without explicit scoped approval.
- Do not change BOOP source, permissions, overlay input boundaries, Home Assistant sockets, or protected checkpoints.
- The physical Shield alone accepts NVIDIA launcher/firmware, Deezer H1, overlay lifecycle, HDR/display, performance, soak, and final remote behaviour.
- Do not install on the physical Shield or grant Android access without a separate explicit request.
- Label emulator evidence local/automated; label physical evidence separately.

---

## File structure

- Create: `$env:USERPROFILE\.android\avd\BOOP_Android_TV_API_<api>.avd` — emulator-owned AVD data; never commit.
- Create: `$env:USERPROFILE\.android\avd\BOOP_Android_TV_API_<api>.ini` — emulator-owned metadata; never commit.
- Create: `docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md` — sanitized local verification record, committed only without addresses, tokens, screenshots, APKs, or raw logs.
- Modify: per-app Windows graphics preference for `emulator.exe`, only after confirmation.

### Task 1: Discover the exact installed SDK components

**Files:**
- Create: none.
- Modify: none.
- Test: installed-package inventory and emulator version.

**Interfaces:**
- Consumes: SDK root `C:\Users\ryank\AppData\Local\Android\Sdk`.
- Produces: `emulator.exe`, `sdkmanager.bat`, `avdmanager.bat`, `adb.exe`, and an installed Android TV system-image identifier.

- [ ] **Step 1: Inspect the SDK with scoped read access**

```powershell
$sdkRoot = 'C:\Users\ryank\AppData\Local\Android\Sdk'
Get-ChildItem -Force $sdkRoot
Get-ChildItem -Recurse -Filter emulator.exe (Join-Path $sdkRoot 'emulator')
Get-ChildItem -Recurse -Filter sdkmanager.bat (Join-Path $sdkRoot 'cmdline-tools')
Get-ChildItem -Recurse -Filter avdmanager.bat (Join-Path $sdkRoot 'cmdline-tools')
Get-ChildItem -Recurse -Filter adb.exe (Join-Path $sdkRoot 'platform-tools')
```

Expected: tool paths or an exact missing/access error, with no machine change.

- [ ] **Step 2: List the installed TV images and current AVDs**

```powershell
& '<sdkmanager.bat>' --list_installed | Select-String 'android-tv|google_apis.*tv|system-images'
& '<emulator.exe>' -version
& '<emulator.exe>' -list-avds
```

Expected: an installed TV image and existing Pixel AVD names. If no TV image exists, report the literal missing package and obtain approval before download.

- [ ] **Step 3: Create a sanitized discovery record**

Record the selected API and package identifier in `docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md`. Omit user paths beyond SDK root, serials, device addresses, tokens, and raw logs.

- [ ] **Step 4: Commit the discovery record**

```powershell
git add docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md
git commit -m 'docs: record Shield emulator prerequisites'
```

Expected: documentation-only commit; never stage `shield-overlay/app/src/main/res/drawable-nodpi/boop_eyes.png`.

### Task 2: Build and boot the 1080p Android TV AVD

**Files:**
- Create: `$env:USERPROFILE\.android\avd\BOOP_Android_TV_API_<api>.avd`.
- Create: `$env:USERPROFILE\.android\avd\BOOP_Android_TV_API_<api>.ini`.
- Modify: the sanitized verification record.
- Test: AVD creation, cold boot, ADB connection, renderer output.

**Interfaces:**
- Consumes: Task 1's Android TV image identifier.
- Produces: AVD `BOOP_Android_TV_API_<api>` and a single online `emulator-*` ADB target.

- [ ] **Step 1: Create the named AVD**

```powershell
$avdName = 'BOOP_Android_TV_API_<api>'
& '<avdmanager.bat>' create avd --force --name $avdName --package '<installed TV image package>' --device 'tv_1080p'
```

Expected: AVD creation without touching existing Pixel AVDs. If `tv_1080p` is unavailable, stop and report compatible TV device IDs; never select a phone profile.

- [ ] **Step 2: Set the AVD hardware graphics keys**

```powershell
$config = Join-Path $env:USERPROFILE '.android\avd\BOOP_Android_TV_API_<api>.avd\config.ini'
Get-Content -LiteralPath $config | Select-String '^hw.gpu.(enabled|mode)='
```

Expected: inspect before change. If settings are absent or different, replace only the `hw.gpu.enabled` and `hw.gpu.mode` lines with `yes` and `auto`; do not append duplicates.

- [ ] **Step 3: Cold boot with automatic host graphics**

```powershell
& '<emulator.exe>' -avd 'BOOP_Android_TV_API_<api>' -no-snapshot -gpu auto
```

Expected: Android TV launcher appears. Do not install BOOP or grant permissions in this task.

- [ ] **Step 4: Verify ADB and renderer**

```powershell
& '<adb.exe>' devices
& '<adb.exe>' shell getprop ro.product.model
& '<adb.exe>' shell dumpsys SurfaceFlinger | Select-String 'GLES|Vulkan|renderer'
```

Expected: one online `emulator-*` target, TV-oriented model output, and renderer information. Software rendering is recorded as unaccelerated and blocks Task 4.

- [ ] **Step 5: Commit boot evidence**

```powershell
git add docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md
git commit -m 'docs: verify Android TV emulator boot'
```

Expected: local/automated evidence only.

### Task 3: Configure and verify the NVIDIA preference

**Files:**
- Modify: Windows per-app graphics preference for `emulator.exe` only with confirmation.
- Modify: sanitized verification record.
- Test: system readback and post-restart renderer report.

**Interfaces:**
- Consumes: Task 2's working AVD and `emulator.exe` path.
- Produces: a verified host hardware renderer, and NVIDIA high-performance process preference if Windows exposes it.

- [ ] **Step 1: Inspect GPU and virtualization state without changing it**

```powershell
Get-CimInstance Win32_VideoController | Select-Object Name,DriverVersion,AdapterRAM
Get-CimInstance Win32_ComputerSystem | Select-Object HypervisorPresent
Get-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform
Get-WindowsOptionalFeature -Online -FeatureName VirtualMachinePlatform
```

Expected: adapter and feature-state data only.

- [ ] **Step 2: Ask for permission before changing per-app graphics settings**

In **Settings > System > Display > Graphics**, add the discovered `emulator.exe` as a Desktop app, choose **Options**, select the NVIDIA-labelled **High performance** GPU, and save.

Expected: only `emulator.exe` receives the preference; no NVIDIA Control Panel global default changes.

- [ ] **Step 3: Restart and re-verify**

```powershell
& '<emulator.exe>' -avd 'BOOP_Android_TV_API_<api>' -no-snapshot -gpu auto
& '<adb.exe>' shell dumpsys SurfaceFlinger | Select-String 'GLES|Vulkan|renderer'
```

Expected: hardware renderer. If it does not name the exact adapter, record “host hardware acceleration” rather than falsely claiming NVIDIA use.

- [ ] **Step 4: Commit GPU evidence**

```powershell
git add docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md
git commit -m 'docs: record emulator GPU acceleration'
```

Expected: documentation only.

### Task 4: Run BOOP emulator smoke tests without replacing physical acceptance

**Files:**
- Modify: sanitized verification record.
- Test: debug build, emulator-only install/launch, remote-first smoke test.

**Interfaces:**
- Consumes: accelerated AVD and current BOOP Shield worktree.
- Produces: local smoke-test evidence separate from physical Shield proof.

- [ ] **Step 1: Build the debug APK without source changes**

```powershell
git status --short
$env:ANDROID_HOME = 'C:\Users\ryank\AppData\Local\Android\Sdk'
& 'C:\Users\ryank\Documents\Codex\2026-09-05\referenced-chatgpt-conversation-this-is-an\work\gradle-9.6.0\bin\gradle.bat' -p .\shield-overlay :app:assembleDebug
```

Expected: debug build success from the nested Shield Gradle project. If the
untracked `boop_eyes.png` affects the build, stop and report it; do not add,
edit, or delete it.

- [ ] **Step 2: Install only on the emulator**

```powershell
& '<adb.exe>' -s '<emulator-serial>' install -r '<BOOP debug APK>'
& '<adb.exe>' -s '<emulator-serial>' shell monkey -p com.boop.shieldoverlay 1
```

Expected: installation succeeds and BOOP launches. Never substitute a physical-device serial.

- [ ] **Step 3: Run remote-first smoke checks**

Verify D-pad focus, Select, Back, BOOP placement, and ordinary input reaching the TV surface. Do not enable microphone, touch capture, notification listener, accessibility, overlay access, or Home Assistant pairing to force a result.

Expected: each outcome is pass, fail, or unsupported by stock Android TV; unsupported Shield-specific behaviour is not treated as an emulator defect.

- [ ] **Step 4: Publish the sanitized final record**

Add this exact statement: “Physical NVIDIA SHIELD validation remains required for Deezer H1, overlay access/lifecycle, HDR/display, extended stability, and release acceptance.”

```powershell
git add docs/superpowers/verification/2026-09-06-shield-emulator-test-lab.md
git commit -m 'docs: record BOOP Android TV emulator smoke test'
git fetch --no-tags origin refs/heads/boop-shield-media-puppetry:refs/remotes/origin/boop-shield-media-puppetry
git push origin HEAD:refs/heads/boop-shield-media-puppetry
git ls-remote origin refs/heads/boop-shield-media-puppetry
git rev-parse HEAD
```

Expected: remote SHA equals local `HEAD`; stop to reconcile if remote advanced and never force-push.

## Plan self-review

- Spec coverage: Tasks 1-3 cover prerequisites, standard 1080p Android TV AVD creation, host-GPU acceleration, and an explicit no-passthrough boundary. Task 4 covers BOOP smoke testing and preserves the physical-Shield release gate.
- Safety coverage: every installation, system graphics change, device installation, permission, and physical-device action is scoped or requires explicit confirmation.
- Placeholder scan: API, paths, serial, and image identifier are outputs of Task 1; they are intentionally discovered rather than invented.
- Interface consistency: Tasks 2-4 consume the AVD name and tool/image paths produced by Task 1.
