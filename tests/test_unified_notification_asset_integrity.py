from hashlib import sha256
from pathlib import Path

EXPECTED = "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"
ASSET = Path("unified/assets/boop-notifications/boop-yellow-hands-approved.png")


def test_notification_hands_are_exact_locked_binary():
    assert ASSET.is_file()
    assert sha256(ASSET.read_bytes()).hexdigest() == EXPECTED
