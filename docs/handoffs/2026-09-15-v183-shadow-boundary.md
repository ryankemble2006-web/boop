# v183: exclude eye shadow from photographic felt

Owner `boop-felt-shadow-boundary-v183`; reviewed source `d06c49358b941b160d17e8a061d6bbafb3fc808c`.
V182 remains visually rejected: user sees two lids on both devices, with a faint line beneath the lid shadow. Vivid felt colour and sharing remain accepted.

## Root cause and change
The coarse white-run detector included several rows of neutral sclera shadow as felt. Recolouring lifted those pixels into a bright curved strip below the actual dark felt lip; blink stretching enlarged the same strip.
After existing narrow-spike rejection, valid columns search up to32rows backward for the last3consecutive dark pixels (peakRGB<=24) and put the material edge immediately after that run. Unresolved columns keep the previous boundary. Feathering ends at the material boundary instead of extending2pixels into sclera shadow.
Original PNG, vivid palette/fabric conversion, shared settings, preview, clock and cap motion are unchanged.

## Validation
Red run34935815116 reproduced shadow intrusion atx250,y340 (coarse edge348) and feather spill. Ten numeric shadow fixtures, existing whole-eye/iris/motion checks, shader compilation and independent source review cover this focused correction. These do not certify appearance; both-device visual verdict remains manual.

## Signed delivery
GitHub run34935869282 passed at the exact reviewed source; artifact10383064244.
Package `com.boop.alpha1`, version183/`1.2.183-png-puppet`.
APK SHA256 `ff71d99a4de7170187858c29de861bab63bcb52436fb4ab2d6a7ea523ad32fd9`.
Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Local artifact hash, package and signer verified. Installed on Shield and Pixel7Pro; actual installed APK hashes/versions match.
Shield16/16 preference files unchanged; phone10/11 unchanged, only notification bookkeeping changed. No manual colour/display/permission change; Pixel10 untouched. V182 rollback hash reverified, v180/v178 artifacts retained.
Shield logged PNG renderer-ready. Fresh private green capture inspected as one frame only; full blink and second-edge disappearance remain unaccepted pending user verdict. Phone immediate renderer log empty; installation verified, visible preview verdict pending.
