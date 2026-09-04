#!/usr/bin/env bash
set -euo pipefail

DEST="${1:?usage: fetch-wake-assets.sh <android-app-dir>}"
CACHE=".cache/boop-wake"
SHERPA_VERSION="1.13.7"
AAR="sherpa-onnx-${SHERPA_VERSION}.aar"
AAR_SHA256="c4ef49e309f24fcee5c106b8a279481aaecaabb078cd37b2cd6e9a62cc8a73c8"
MODEL_ARCHIVE="sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2"
MODEL_DIR="${CACHE}/sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile"
BASE="https://github.com/k2-fsa/sherpa-onnx/releases/download"

mkdir -p "$CACHE" "$DEST/libs" "$DEST/src/main/assets/boop-kws"

if [ ! -f "$CACHE/$AAR" ]; then
  curl -fL --retry 3 -o "$CACHE/$AAR" "$BASE/v${SHERPA_VERSION}/$AAR"
fi
printf '%s  %s\n' "$AAR_SHA256" "$CACHE/$AAR" | sha256sum -c -

if [ ! -f "$CACHE/$MODEL_ARCHIVE" ]; then
  curl -fL --retry 3 -o "$CACHE/$MODEL_ARCHIVE" "$BASE/kws-models/$MODEL_ARCHIVE"
fi
curl -fsSL "$BASE/kws-models/checksum.txt" -o "$CACHE/checksum.txt"
MODEL_SHA256="$(awk -v name="$MODEL_ARCHIVE" '$1 == name {print $2; exit} $2 == name {print $1; exit}' "$CACHE/checksum.txt")"
test -n "$MODEL_SHA256"
printf '%s' "$MODEL_SHA256" | grep -Eq '^[0-9a-fA-F]{64}$'
printf '%s  %s\n' "$MODEL_SHA256" "$CACHE/$MODEL_ARCHIVE" | sha256sum -c -

if [ ! -d "$MODEL_DIR" ]; then
  tar -xjf "$CACHE/$MODEL_ARCHIVE" -C "$CACHE"
fi

cp "$CACHE/$AAR" "$DEST/libs/$AAR"
for file in \
  encoder-epoch-12-avg-2-chunk-16-left-64.int8.onnx \
  decoder-epoch-12-avg-2-chunk-16-left-64.onnx \
  joiner-epoch-12-avg-2-chunk-16-left-64.int8.onnx \
  tokens.txt; do
  test -s "$MODEL_DIR/$file"
  cp "$MODEL_DIR/$file" "$DEST/src/main/assets/boop-kws/$file"
done

test -s wake-assets/boop-kws/keywords.txt
cp wake-assets/boop-kws/keywords.txt "$DEST/src/main/assets/boop-kws/keywords.txt"
