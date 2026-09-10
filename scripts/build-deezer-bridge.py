"""Compile the small, source-owned ADB media bridge into the Unified build.

The payload is built from Java on every CI build, never downloaded or committed
as an opaque binary. Its SHA256 is verified on the target before execution.
"""
import base64
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parents[1]
sdk = Path(os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT") or
           str(Path(os.environ.get("LOCALAPPDATA", "")) / "Android/sdk"))
java_home = os.environ.get("JAVA_HOME")
suffix = ".exe" if os.name == "nt" else ""
def java_tool(name):
    return str(Path(java_home) / "bin" / (name + suffix)) if java_home else shutil.which(name)

android = sdk / "platforms/android-36/android.jar"
d8 = sdk / "build-tools/36.0.0/lib/d8.jar"
if not android.is_file() or not d8.is_file():
    raise SystemExit("Install Android SDK platform 36 and build-tools 36.0.0 before materializing Unified")
output = root / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/DeezerBridgePayload.java"
output.parent.mkdir(parents=True, exist_ok=True)
with tempfile.TemporaryDirectory(prefix="boop-deezer-bridge-") as temp:
    work = Path(temp)
    subprocess.run([java_tool("javac"), "-encoding", "UTF-8", "-cp", str(android), "-d", str(work),
                    str(root / "unified/deezer-bridge/DeezerMediaBridge.java")], check=True)
    jar = work / "bridge.jar"
    subprocess.run([java_tool("java"), "-cp", str(d8), "com.android.tools.r8.D8", "--min-api", "29",
                    "--output", str(jar), *map(str, work.rglob("*.class"))], check=True)
    payload = jar.read_bytes()
    digest = hashlib.sha256(payload).hexdigest()
    encoded = base64.b64encode(payload).decode("ascii")
    output.write_text("package com.boop.alpha1;\nfinal class DeezerBridgePayload {\n"
                      + ' static final String SHA256="' + digest + '";\n'
                      + ' static final String BASE64="' + encoded + '";\n}\n', encoding="utf-8")
    print("Source-built Deezer bridge SHA256:", digest)
