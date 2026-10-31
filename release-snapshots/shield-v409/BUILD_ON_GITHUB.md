# GitHub build and signing

The existing Build and sign BOOP Shield workflow selects v409, verifies its Java source delta against v408, compiles the equivalent narrowly scoped launcher configuration method patch with checksum-pinned Apktool2.12.1, and changes only the version manifest and classes3.dex. Other APK entries (resources/assets/native libraries/other dex) remain byte-identical. No Gradle rebuild or metadata is introduced.

GitHub then signs with the existing permanent repository key, checks package/version/alignment/certificate continuity, and uploads the signed APK. No local signing key is created or used.
