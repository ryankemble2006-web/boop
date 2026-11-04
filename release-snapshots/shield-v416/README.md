# BOOP Shield v416

Display-isolation fix for BOOP Shield (`1.2.416-shield`). Wall v222 is unchanged.

BOOP no longer forces Shield-wide display size or density during wake, resume or permission recovery. Existing app-local adaptive layout remains unchanged.

Reproduce: `python build_display_isolation.py --tool <apktool-2.12.1.jar>`. The builder pins the baseline/tool and verifies all DEX classes and changed APK entries. GitHub supplies permanent signing.

See DISPLAY_FIX_RECEIPT.md for current evidence and hardware acceptance boundaries.
