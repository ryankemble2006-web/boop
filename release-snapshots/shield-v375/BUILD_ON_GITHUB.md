# Locally build and GitHub-sign BOOP Shield

Build the APK from this snapshot on the local workstation. Send that APK to the
GitHub signing handoff so the existing BOOP development key in GitHub secrets
can sign it. Verify the returned APK's package, version, alignment, and signer
before installing it on the NVIDIA Shield TV.

The signed APK and receipt should identify the exact committed source revision
used for the local package.
