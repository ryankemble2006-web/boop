# Antigravity Rules for BOOP & Shield Launcher

## Ryan's Workflow Contract ("Always, To All of Em, Forever")
- **Assume All Permissions as YES**: Proceed autonomously without waiting for approvals on commands, edits, git operations, or installations.
- **GitHub Does Everything**: GitHub Actions handles compiling and signing the APK with repository secrets. Push commits to trigger CI; do not create local replacement signing keys.
- **No Visual Checks**: Strictly headless. No visual checks, no browser previews, no generative UI widgets, no UI simulations. Pure code, commit, push, CI, deploy.
- **Install Signed APK Directly to Nvidia Shield**:
  - Shield Network ADB: `10.58.1.248:5555` (model: `SHIELD Android TV` / `mdarcy`).
  - Tool: `adb` (`C:\Users\ryank\AppData\Local\Android\Sdk\platform-tools\adb.exe`).
  - Automated deployment: Fetch latest signed artifact from GitHub CI via `gh run download` and stream-install via `adb -s 10.58.1.248:5555 install -r <apk>`.
- **Token Efficiency & Local Plugins First**:
  - Prioritize local tools, local plugins, and direct filesystem/CLI access over GitHub API queries during development to preserve token budget.
  - Limit GitHub flow strictly to pushing commits, triggering CI signing, and downloading signed APKs.
- **BOOP Architecture Constraints**:
  - Local Home Assistant WebSocket first.
  - Remote-friendly navigation (D-pad/Up/Down/Left/Right).
  - Protect proven checkpoints (`checkpoint-shield-home-f8e8135`, `checkpoint-shield-routines-3fa18c6`).
- **Music State Recovery (Deezer Flow)**:
  - If ADB testing, package restarts, or crashes interrupt music, restore Deezer Flow silently via `input keyevent --longpress 3`.

