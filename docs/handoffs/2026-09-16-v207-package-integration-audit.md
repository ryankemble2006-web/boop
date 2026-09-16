# v207 package integration audit

Owner: `boop-wall-shield-split-v207`. Base: accepted v206 source `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`, preserving latest owner documentation through `841458b8bbc53773d16a18bb0359de1c0550f5e2`.

## Package identity and source namespaces

Wall restores `com.boop.alpha1`; Shield restores `com.boop.shieldoverlay`. Java namespaces are deliberately unchanged. Current materialized explicit Activity intents use the runtime package name plus the existing fully qualified class name. Class literals, action/extra constants and the HA mobile-app metadata ID are not Android application IDs and must not be globally renamed. The shared BuildConfig is used for DEBUG and existing relay constants, not a stale generated APPLICATION_ID.

The generated common manifest fully qualifies relative component names. Only application shells advertise LAUNCHER/HOME; Shield also advertises Leanback. Android's default HOME choice remains user-controlled. Shared services, receivers and permission declarations retain their existing access boundaries. Both applications disable backup; no shared UID, data migration or auto-grant was introduced.

The launcher app-list self-filter was the one materialized Java reference that depended on the old installed identity. Its PackageManager-only repository does not own a Context. It now compares ApplicationInfo.uid to Process.myUid; a Java harness executes the real generated repository for both BOOP identities, foreign apps, sorting and null ActivityInfo handling. No accepted Home/card/voice source was modified for this repair.

## Johnny producer and CURRENT consumer

Producer source and caller guard remain byte-identical to v206: `unified/JohnnyStateProvider.java` / `JohnnyStatePolicy.java`. The provider accepts only the separately signed `local.johnnycastaway.halab` package with the pinned caller certificate and a fixed read-only snapshot request. Caller checks precede Binder identity clearing. Credentials, arbitrary entities and HA commands are not exposed.

Current consumer branch `johnny-ha-native-v8`, live head `0381b8b8c929f1bf0fd9bf12ce43df90258c7721`, was inspected directly. `johnny-ha/java/local/johnnycastaway/shield/JohnnyStateClient.java` calls `content://com.boop.alpha1.johnny_states` through ContentResolver, rather than resolving an explicit BOOP Activity or binding by its application ID. Its CURRENT manifest is versionCode 15 / `1.14-reborn-ha-fast-poll`, targetSdk 30, and declares both `<provider android:authorities="com.boop.alpha1.johnny_states" />` and the historical package within queries. The provider-authority query therefore preserves discovery when the owning APK changes identity.

Only Shield declares the compatibility authority, along with the new canonical `com.boop.shieldoverlay.johnny_states` alias. Wall does not compete for the legacy authority. No Johnny code, APK, manifest, signing configuration or branch was changed. Existing real-device Johnny state response after fresh BOOP sign-in remains a joint test, not claimed from this source audit.

## Setup and frozen implementation

A fixed shell identity determines the body even if historical override preferences exist. The first real setup page exposes current HA, room, Voice and access actions, without asking for a device/profile. An app-private intro marker remains false on entry/reentry and is written only after the user presses Continue; failed persistence does not advance setup. Android notification/overlay/assistant/default-HOME confirmations remain genuine user actions. No old setup flag, credential or voice model is restored.

The split uses one shared assistant source/resource tree and existing animation/launcher/TV libraries. The materialization report hashes every generated main Java/asset file before/after the shell step. Its only permitted shared-source difference is the package-independent self-filter. Voice, native runtime files, appearance/artwork and accepted Home/card layout are otherwise unchanged. APK verification must separately validate both packaged identities, exact signer and accepted content. The 12-second Voice investigation remains deferred, not fixed.

## Current RED evidence and bounded repairs

Initial focused run `35106866646` was RED before implementation: three expected missing split/chooser/identity contracts failed and protected Voice/Home/artwork preservation passed. The first implementation run `35109154640` exposed the erroneous Context-field assumption in the new materializer; the production repository was read and the fix uses UID instead.

Full run `35109154846` passed the actual inherited checks through 1,444 music-worker assertions, then correctly stopped at its inherited production-change allowlist. Only seven intentional split files are added to that exact allowlist; no functional assertion or protected-source check is removed. New full green build/signing evidence is still required before deployment.

## Device boundary

Yoga temporarily stopped responding during the read-only device audit and later answered ping again. No uninstall, installation, permission change or setup navigation was performed during that interruption. Both existing recovery APKs remain retained privately. Recheck actual devices, recovery and both verified replacement APKs before deployment. Pixel 10 remains outside all targeted operations.
