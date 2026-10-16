# Build and sign BOOP Shield on GitHub

Push the new Shield snapshot to the `shield-v325-live` branch. The
`build-wall-shield-split.yml` GitHub Actions workflow selects the newest
versioned snapshot, runs its unit tests, builds the Shield APK, signs it with
the existing BOOP development key in GitHub Actions secrets, verifies the
permanent signer, and uploads the signed APK and build receipt.

The same workflow can be started manually with `workflow_dispatch`. No local
Gradle build is needed for the GitHub signing path.
