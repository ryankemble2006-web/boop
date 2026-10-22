# Build, test, and sign BOOP Shield on GitHub

The `Build and sign BOOP Shield` workflow runs for changes to a versioned Shield
source snapshot. It materializes the bundled Sherpa AAR into a temporary local
Maven repository, runs `:assistant-lib:testDebugUnitTest`, builds
`:shield-app:assembleDebug`, strips build-tool metadata from the APK, and signs the APK
with the permanent GitHub signing secrets. It verifies package identity, version,
alignment, certificate continuity, and guarantees zero Gradle artifacts inside the APK
before uploading the APK and build receipt as a workflow artifact.
