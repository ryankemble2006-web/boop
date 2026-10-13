# Build, test, and sign BOOP Shield on GitHub

The `Build and sign BOOP Shield` workflow runs for changes to a versioned Shield
source snapshot. It materializes the bundled Sherpa AAR into a temporary local
Maven repository, runs `:assistant-lib:testDebugUnitTest`, builds
`:shield-app:assembleDebug`, and signs the APK with the existing GitHub signing
secrets. It verifies package identity, version, alignment, and certificate
continuity before uploading the APK and build receipt as a workflow artifact.

Download `BOOP-Shield-v341-signed` from the successful Actions run to install the
APK. Verify the Lyrics title appears below the artist and retains its one-pass
scroll behavior on the physical Shield after installation.
