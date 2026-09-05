from pathlib import Path
import sys


MARKER = "// BOOP_DEV_SIGNING"
INJECTION = r'''android {
    // BOOP_DEV_SIGNING
    signingConfigs {
        boopDev {
            storeFile file(System.getenv('BOOP_SIGNING_STORE_FILE'))
            storePassword System.getenv('BOOP_DEV_STORE_PASSWORD')
            keyAlias 'boop-dev'
            keyPassword System.getenv('BOOP_DEV_KEY_PASSWORD')
        }
    }
    buildTypes {
        debug {
            signingConfig signingConfigs.boopDev
        }
    }
'''


def patch(path: Path) -> None:
    text = path.read_text(encoding="utf-8")
    if MARKER in text:
        return
    marker = "android {"
    if marker not in text:
        raise SystemExit(f"android block not found in {path}")
    path.write_text(text.replace(marker, INJECTION, 1), encoding="utf-8")


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("usage: apply_boop_dev_signing.py <app/build.gradle>")
    patch(Path(sys.argv[1]))


if __name__ == "__main__":
    main()
