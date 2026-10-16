# Build BOOP Shield locally

The repository includes a Gradle wrapper pinned to Gradle 9.6.0 with its distribution checksum. Install Java 17 and Android SDK platform/build tools 36 once; set JAVA_HOME and ANDROID_HOME (or ANDROID_SDK_ROOT) if the standard locations are not detected.

From the repository root, run:

    .\tools\build-shield.ps1

The command selects the newest release-snapshots/shield-vN, materializes the bundled Sherpa AAR into an ignored local Maven directory, runs the Shield unit tests, builds and verifies the APK on this computer, then requests the permanent signature from GitHub. The local signing key is temporary and discarded after the build.

GitHub only signs the APK produced here. The script sends that APK and its commit/hash receipt to a temporary draft release, pushes a one-time signature trigger tag on the same commit, downloads and verifies the signed APK, then deletes the draft and trigger tag. The final APK and receipts remain under the ignored boop-build/shield-vN/<commit>/ directory.

To run only the local tests and build without using GitHub:

    .\tools\build-shield.ps1 -BuildOnly

For a signature request, commit and push the current Shield release branch first. The script refuses to continue if the tracked working tree is dirty or the remote branch does not point to the same commit as the local build.
