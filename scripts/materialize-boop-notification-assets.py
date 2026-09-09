#!/usr/bin/env python3
from hashlib import sha256
from pathlib import Path
import shutil

EXPECTED_SHA256 = "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"
ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "unified/assets/boop-notifications/boop-yellow-hands-approved.png"
DESTINATION = (
    ROOT
    / "boop-build/BOOP-Alpha1/app/src/main/res/drawable-nodpi/boop_notification_hands.png"
)


def digest(path: Path) -> str:
    return sha256(path.read_bytes()).hexdigest()


def main() -> None:
    if not SOURCE.is_file():
        raise SystemExit(f"Missing locked notification hands: {SOURCE}")
    source_digest = digest(SOURCE)
    if source_digest != EXPECTED_SHA256:
        raise SystemExit(
            "Locked notification hands changed: "
            f"expected {EXPECTED_SHA256}, got {source_digest}"
        )

    DESTINATION.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(SOURCE, DESTINATION)

    destination_digest = digest(DESTINATION)
    if destination_digest != EXPECTED_SHA256:
        raise SystemExit(
            "Materialized notification hands are not byte-identical: "
            f"expected {EXPECTED_SHA256}, got {destination_digest}"
        )
    print(f"Materialized exact notification hands: {destination_digest}")


if __name__ == "__main__":
    main()
