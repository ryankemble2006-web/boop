# Build BOOP Shield locally and sign on GitHub

From the repository root, run tools/build-shield.ps1. It runs the Shield unit
tests, builds the APK, and verifies its package, version, alignment, and local
signature on this computer.

GitHub only applies the permanent BOOP signature to that locally built APK.
The script uploads it with a commit/hash receipt to a temporary draft release,
starts the signer with a one-time trigger tag, downloads the signed artifact,
verifies the permanent certificate, and removes the draft and trigger tag.

See BUILD_SHIELD.md in the repository root for setup, output paths, and the
BuildOnly option.
