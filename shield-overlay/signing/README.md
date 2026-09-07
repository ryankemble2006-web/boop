# BOOP development signing

This directory contains only public signing metadata. The private BOOP development keystore is stored outside Git and injected into GitHub Actions through repository secrets.

Expected development certificate SHA-256 is stored in `boop-dev-cert-sha256.txt` and CI must reject any APK signed by a different certificate.

Never commit `.jks`, `.keystore`, passwords, or base64-encoded private key material here.
