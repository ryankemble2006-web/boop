# BOOP Shield v416 display isolation

- Baseline: v415 unsigned APK SHA-256 `1a9c06292b404ac2af10b4d601d5a855d634773d138c7bfb0a5b77e3bb191ec5`.
- Removes the wake/resume global display writer and size/density writes from permission recovery. No-op compatibility hook retains lifecycle callers.
- Preserves app-local scaling, remote reconnect, accessibility, launcher, and banners.
- Locally assembled and decoded again: 814 classes retained in classes3.dex; only AndroidManifest.xml and classes3.dex change inside the APK. Signature metadata is removed for GitHub signing.
- Regression: both gates fail against v415 and pass against v416 source/decoded DEX. All nine baseline DEX files scanned: only the two targeted display command strings occur.
- Recovery confirmed by screenshot: EastEnders fullscreen with 1920x1080 logical UI / stock 320 DPI while HDMI remains 3840x2160.
- Wake/resume/reboot and native 4K UI acceptance are pending. No display or navigation changes during viewing. The fixed application does not enforce a resolution; Android/NVIDIA retain control.

Build with `python build_display_isolation.py --tool <apktool-2.12.1.jar>` and run `python test_display_isolation.py display-verified.smali shield-home-lib/src/main/java/com/boop/shieldhome/ShieldSelfGrant.java`.

The existing GitHub workflow aligns and signs the locally audited APK; it does not run Gradle or rebuild it. Signing and installation are recorded separately after completion.
