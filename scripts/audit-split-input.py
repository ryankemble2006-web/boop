#!/usr/bin/env python3
"""Report package-dependent active source on GitHub; never reads device data."""
from pathlib import Path
import re

root = Path("boop-build/BOOP-Alpha1")
assert (root / "app/src/main/AndroidManifest.xml").is_file(), "Materialize the current app first"
rows = []
for path in sorted(root.rglob("*.java")):
    if "/src/main/" not in path.as_posix():
        continue
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        if line.strip().startswith(("package ", "import ")):
            continue
        if "BuildConfig." in line or re.search(r'["\']com\.boop\.(alpha1|shieldoverlay|unified)', line):
            rows.append(f"{path.relative_to(root)}:{number}: {line.strip()}")
for path in [root / "settings.gradle", *sorted(root.glob("*/src/main/AndroidManifest.xml"))]:
    rows.append("\n=== " + str(path.relative_to(root)) + " ===\n" + path.read_text(encoding="utf-8"))
report = "\n".join(rows)
Path("split-input-audit.txt").write_text(report, encoding="utf-8")
print(report)
