import hashlib
import os
import re
import unittest
import zipfile
from pathlib import Path

SITE = Path(__file__).resolve().parents[1]

EXPECTED = {
    "index.html",
    "privacy.html",
    "styles.css",
    "site.js",
    "404.html",
    ".htaccess",
    "README-FIRST.txt",
    "assets/boop-eyes.png",
    "assets/boop-yellow-hands.png",
    "assets/boop-headphones.png",
}

EYE_SHA256 = "ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22"
HANDS_SHA256 = "26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1"


def sha256(path: Path) -> str:
    if not path.is_file():
        raise AssertionError(f"missing file: {path}")
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


class BoopSiteContract(unittest.TestCase):
    def read(self, rel: str) -> str:
        path = SITE / rel
        self.assertTrue(path.is_file(), f"missing file: {rel}")
        return path.read_text(encoding="utf-8")

    def test_expected_deploy_files_exist(self):
        missing = sorted(rel for rel in EXPECTED if not (SITE / rel).is_file())
        self.assertEqual([], missing, f"missing deploy files: {missing}")

    def test_protected_assets_keep_exact_hashes(self):
        self.assertEqual(EYE_SHA256, sha256(SITE / "assets/boop-eyes.png"))
        self.assertEqual(HANDS_SHA256, sha256(SITE / "assets/boop-yellow-hands.png"))

    def test_main_page_contains_public_story_and_local_assets(self):
        html = self.read("index.html")
        for phrase in (
            "BOOP works FOR you.",
            "Works FOR BOOP",
            "Current prototype",
            "Concept",
            "privacy.html",
            "github.com/ryankemble2006-web/boop",
            "assets/boop-eyes.png",
            "assets/boop-yellow-hands.png",
            "assets/boop-headphones.png",
            "Natural voice remains unresolved",
        ):
            self.assertIn(phrase, html)

    def test_no_tracking_framework_cdn_or_apk_autolinks(self):
        html = self.read("index.html")
        privacy = self.read("privacy.html")
        js = self.read("site.js")
        combined = "\n".join((html, privacy, js)).lower()
        for forbidden in (
            "google-analytics",
            "gtag(",
            "fonts.googleapis",
            "raw.githubusercontent",
            "<iframe",
            "latest/download",
        ):
            self.assertNotIn(forbidden, combined)
        self.assertIsNone(re.search(r'href=["\'][^"\']+\.apk(?:[?#][^"\']*)?["\']', combined))
        self.assertNotRegex(html, r'<script[^>]+src=["\']https?://')
        self.assertNotRegex(html, r'<(?:img|source)[^>]+src=["\']https?://')

    def test_internal_nav_anchors_resolve(self):
        html = self.read("index.html")
        ids = set(re.findall(r'\bid=["\']([^"\']+)["\']', html))
        anchors = set(re.findall(r'href=["\']#([^"\']+)["\']', html))
        unresolved = sorted(anchor for anchor in anchors if anchor not in ids)
        self.assertEqual([], unresolved, f"unresolved anchors: {unresolved}")

    def test_future_hardware_is_explicitly_conceptual(self):
        html = self.read("index.html")
        future = re.search(r'<section[^>]+id=["\']future["\'][\s\S]*?</section>', html, re.I)
        self.assertIsNotNone(future)
        block = future.group(0)
        self.assertGreaterEqual(block.count("Concept"), 3)
        self.assertIn("not products for sale", block.lower())

    def test_javascript_is_progressive_enhancement(self):
        html = self.read("index.html")
        for section_id in ("meet", "bodies", "does", "works-for-boop", "privacy", "accessibility", "future", "workshop"):
            self.assertRegex(html, rf'<section[^>]+id=["\']{re.escape(section_id)}["\']')
        self.assertNotIn("document.write", self.read("site.js"))

    def test_reduced_motion_contract_exists(self):
        css = self.read("styles.css").lower()
        self.assertIn("prefers-reduced-motion", css)
        self.assertIn("reduce", css)

    def test_zip_is_root_ready_when_supplied(self):
        zip_path = os.environ.get("BOOP_SITE_ZIP")
        if not zip_path:
            self.skipTest("BOOP_SITE_ZIP not supplied")
        with zipfile.ZipFile(zip_path) as zf:
            names = set(zf.namelist())
        self.assertIn("index.html", names)
        self.assertIn(".htaccess", names)
        self.assertIn("assets/boop-eyes.png", names)
        self.assertFalse(any(name.startswith("site/") for name in names))
        self.assertFalse(any("tests/" in name or name.endswith("test_site.py") for name in names))


if __name__ == "__main__":
    unittest.main()
