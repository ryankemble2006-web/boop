# v182: original PNG eyelids and coloured-felt seam repair

## User verdict driving this candidate
V181 vivid felt colours are physically accepted; preview and sharing remain accepted. V181 blink incorrectly showed a second eyelid under the original. Both lids showed narrow dark scars when green, concealed by charcoal.

## Implementation
Owner `boop-moving-png-lids-v182`, source `66cc6d44e61339bbaeade1a9c67be88f088154ce`.
The existing photographic cap now stretches from its crown to its moving lower edge. The separate hidden-felt sampler is removed from active rendering. Original PNG bytes are unchanged. Bright fabric conversion, palette, settings, sharing, preview and motion clock remain unchanged.
The original white-run boundary detector mistook bright felt fibres for eye whites at columns242-243 and1280-1286. A bounded21-column median from immutable raw measurements rejects only valid spikes greater than30pixels. Normal boundary samples and absent-eye columns remain unchanged. Iris hue coverage is aligned to the photographic iris centres.

## Evidence
Red34934724021 reproduced missing original-lid motion/top anchors. Red34934394559 reproduced iris hue leakage. Red34934865386 reproduced the photographed scar at column242/edge146.
Independent source review cleared66cc6d4. No hosted visual test or local app build. Actual blink appearance and disappearance of both scars require user device verdict.

## Delivery
GitHub run34934919950 passed at the reviewed source; artifact10382263898.
Permanent-signed `com.boop.alpha1` version182 / `1.2.182-png-puppet`.
APK SHA256 `c65e5613417dbd709b6a112d69582f30d48b64d419db2f0d4c5989324b047f39`.
Signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Installed on Shield and Pixel7Pro; actual installed version and APK hashes match. Shield16/16 preference files unchanged; phone10/11 unchanged, only notification bookkeeping differs. Pixel10 untouched. v180 rollback retained and hash-verified.
Shield renderer-ready logged. Private joint Shield capture shows a single green cap without the previously visible narrow dark cuts; this is an assistant observation of one frame, not physical acceptance of the animation. Phone visual verdict pending.
