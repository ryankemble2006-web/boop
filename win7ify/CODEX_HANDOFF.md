# BOOP Win7ify: Codex advanced-phase handoff

## Resume the actual application

Repository `ryankemble2006-web/boop`, branch `boop-win7ify-v01`, source `win7ify/`.
Read live main rules, this branch's SESSION_HANDOFF.md, STATUS.md and MEMORY.md;
fetch/check live HEAD and use the existing isolated worktree. Do not touch the
Android Unified/rebuild/Shield/Wall/Launcher/Turbo app lineages or permanent signer.

Current 0.2 source `ee744dbb8f1bae6851916801eed6163a67541344` built successfully in
run `34674142722`, artifact `10291618453`. EXE SHA256
`ab65fe6c7b3891cf3a31fac40a9edca5a7bce80226b397c92d65432706546f67`.
A later documentation commit does not change those built bytes.

## Already implemented, do not reinvent

The actual Windows 7-style Start menu was brought forward after the user rejected
the lack of visible change in 0.1.1. 0.2 embeds the official pinned Open-Shell 4.4.198
installer and installs only StartMenu. It sets Win7, Windows Aero menu skin, the
replacement Start button, classic mouse/Windows-key behavior, Shift fallbacks and
left alignment. Open-Shell's own settings export and a real runtime window are
verified. It is NOT full desktop Aero or a replacement taskbar.

BOOP front page: install, open menu, put Windows 11 back, separate desktop-settings
page, stable E2xx stage errors, full local diagnostic log, copy/save report and
embedded third-party notices. No startup Apply or surprise Explorer restart.

Core uses IRegistryStore, BackupService, Win7ifyService and an injectable IOpenShellHost.
OpenShellSession owns the atomic journal and separate original profile backup.
Interrupted setup/undo, pre-existing ownership, changed-version refusal, blocked
values, corrupt data and first-baseline preservation have functional tests.
BackupService.RestoreAll(keepBackup: true) retains the menu backup until installer
removal also completes. The older backup-v1.json remains independent.

## Evidence and immediate target gate

41 functional checks and real disposable install/profile export/repeat setup,
standalone menu lifecycle, normal Explorer-hook menu start/repeated open/graceful
stop/owned uninstall/repeated undo and diagnostics pass on hosted Windows Server.
No screenshot/golden/pixel/appearance acceptance tests. The real E241 close race
was fixed by dismissing the open vendor menu first and waiting for forwarded
commands, then using bounded official -exit requests. Do not reintroduce process kills.

The exact GUI and read-only command diagnostics passed on Yoga, with its original
backup unchanged. It is copied to the Desktop. Yoga's remote process was not elevated;
installation still requires normal Windows approval. Do not claim target shell or
visual acceptance from the hosted-server result. User's Windows 11 Insider build
was 26220.9343 during the preceding physical test; recheck current build.

## Protected boundaries

- Preserve existing compatible Open-Shell and original preferences; never uninstall it.
- Remove only the exact pinned installation recorded as owned at the exclusive location.
- Never replace first backups with current values or delete recovery after partial failure.
- No UAC/SmartScreen bypass, permission rewrites, security/activation/update tampering,
  protected Windows binary patching, root driver tricks or unwanted installer features.
- Approved BOOP eyes stay byte-locked at SHA256
  ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22.
- Keep free/non-commercial distribution and full upstream notices. Upstream's source
  and asset/trademark terms differ. Both EXEs are unsigned; hashes are not signatures.
- Review/test/publish scoped changes and verify live HEAD. CI and physical acceptance differ.

## Advanced backlog after target verification

1. Deeper Windows 7 taskbar fidelity and window chrome, evaluated against the installed
   Windows shell rather than assumed from registry folklore. Do not erase the current
   working menu/recovery layer while experimenting.
2. Aero/glass across the desktop, useful Explorer navigation/command affordances and
   lawful user-supplied/independently licensed theme assets. The current Aero is menu-only.
3. Compatibility reporting for Insider/release builds and pre-existing legacy Classic
   Shell, 32-bit or unusual installs. Refuse unsafe ownership assumptions. The current
   host recognizes the pinned normal 64-bit Program Files installation.
4. True Windows 11 VM/Sandbox tests, multiple-user and signed-out/reboot lifetime cases,
   stalled installer recovery and manual target visual acceptance. Existing hosted tests
   are useful but are not the full Windows 11 release matrix.
5. Release hardening: Authenticode signing separate from Android keys, durable release
   artifacts, and updating deprecated GitHub action runtimes without changing app scope.
6. Review 0.1 experimental context-menu empty-key ownership: schema v1 does not record
   whole-key existence, so do not claim complete key cleanup. Keep off by default.

Tests: dotnet run --project win7ify/tests/Win7ify.Tests -c Release
and dotnet run --project win7ify/tests/Win7ify.ShellTests -c Release.
Prepare-Payload.ps1 obtains/verifies the pinned upstream EXE before publishing.
Test-Integration.ps1 and Test-HookedMenu.ps1 are guarded for disposable GitHub runners;
never remove that guard just to run their destructive cycles on a personal desktop.
