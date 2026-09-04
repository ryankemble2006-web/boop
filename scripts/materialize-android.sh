#!/usr/bin/env bash
set -euo pipefail
rm -rf boop-build
unzip -q BOOP-Alpha1-project.zip -d boop-build
ROOT=boop-build/BOOP-Alpha1
MAIN="$ROOT/app/src/main/java/com/boop/alpha1"
TEST="$ROOT/app/src/test/java/com/boop/alpha1"
mkdir -p "$MAIN" "$TEST"
cp source/*.java "$MAIN"/
cp source/AndroidManifest.xml "$ROOT/app/src/main/AndroidManifest.xml"
cp source/app-build.gradle "$ROOT/app/build.gradle"
if compgen -G 'source-test/*.java' > /dev/null; then
  cp source-test/*.java "$TEST"/
fi
