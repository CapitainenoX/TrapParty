#!/usr/bin/env bash
# Build du resource pack TrapParty : zip + sha1 affiché.
set -euo pipefail
cd "$(dirname "$0")/.."

PACK_DIR="src/main/resources/resourcepack"
OUT="trapparty-pack.zip"

if [ ! -d "$PACK_DIR" ]; then
  echo "Resource pack folder not found: $PACK_DIR"
  exit 1
fi

rm -f "$OUT"
(cd "$PACK_DIR" && zip -r -9 -X "../../../../$OUT" . -x "*.DS_Store" >/dev/null)

SHA1=$(sha1sum "$OUT" | awk '{print $1}')
SIZE=$(du -h "$OUT" | awk '{print $1}')

echo ""
echo "Built : $OUT  ($SIZE)"
echo "SHA-1 : $SHA1"
echo ""
echo "Mets ces valeurs dans plugins/TrapParty/config.yml :"
echo ""
echo "resource-pack:"
echo "  enabled: true"
echo "  url: \"https://your-cdn.example/trapparty-pack.zip\""
echo "  sha1: \"$SHA1\""
