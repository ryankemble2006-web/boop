# SHIELD TURBO handoff

Branch: `shield-turbo-v01`
Package: `com.boop.shieldturbo`
Candidate: v0.1.0 / versionCode 1

GitHub-only chat-mode development. Independent from BOOP runtime. Current source implements read-only device, memory, storage, CPU/sysfs, thermal, network and privilege capability analysis plus a remote-first TV dashboard. Dedicated CI uses the repository's existing secret-backed `boop-dev` signer and verifies the known signer certificate digest. Physical Shield acceptance is pending and must not be inferred from CI.
