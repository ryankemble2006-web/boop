# BOOP private development signing

BOOP Shield (`com.boop.shieldoverlay`) and BOOP Wall (`com.boop.alpha1`) use one stable private development signing identity in CI so development APKs can update in place after one intentional migration reinstall.

## One-time migration

The previously built private debug APKs were signed with ephemeral runner debug keys. Android cannot update those installs with a new certificate. Clear data/uninstall each affected private BOOP app once, then install the first APK signed by the stable BOOP development key. After that, keep this same development signer for private builds.

Do not use this key for a future public or Play release.

## GitHub Actions secrets

The private keystore is never committed. Store only these repository Actions secrets:

- `BOOP_DEV_KEYSTORE_B64` — base64 of the private `boop-dev.jks`
- `BOOP_DEV_STORE_PASSWORD` — keystore password
- `BOOP_DEV_KEY_PASSWORD` — `boop-dev` key password

The alias is fixed as `boop-dev`.

CI decodes the keystore into `${RUNNER_TEMP}` for the build, signs the debug APK, then verifies the resulting signer against the committed public SHA-256 certificate fingerprint in:

`shield-overlay/signing/boop-dev-cert-sha256.txt`

The committed fingerprint is public metadata and contains no private key material.

## Local safety

Private signing files are ignored by `.gitignore` (`*.jks`, `*.keystore`, `.local-signing/`). Keep an offline backup of the private dev keystore and both passwords. Losing the private key would force another uninstall/reinstall migration for development devices.
