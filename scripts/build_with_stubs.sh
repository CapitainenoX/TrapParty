#!/usr/bin/env bash
# Build TrapParty offline using the local API stubs (for envs without
# access to repo.papermc.io / Vault / PAPI / MV repos).
# For a "real" deployment build, just run `mvn -B package` instead.
set -euo pipefail
cd "$(dirname "$0")/.."

STUB="stubs/bukkit-stub-26.1.2.jar"
[ -f "$STUB" ] || { echo "Stubs missing. Run: python3 stubs/gen.py && javac --release 21 -d stubs/out \$(find stubs/src -name '*.java') && (cd stubs/out && jar cf ../bukkit-stub-26.1.2.jar .)"; exit 1; }

for art in "io.papermc.paper:paper-api:26.1.2.build.65-stable" \
           "org.mvplugins.multiverse.core:multiverse-core:5.6.1" \
           "com.github.MilkBowl:VaultAPI:1.7" \
           "me.clip:placeholderapi:2.11.6"; do
  g=${art%%:*}; rest=${art#*:}; a=${rest%:*}; v=${rest##*:}
  mvn -B -q install:install-file -Dfile="$STUB" \
    -DgroupId="$g" -DartifactId="$a" -Dversion="$v" -Dpackaging=jar >/dev/null
done

mvn -B package
echo ""
echo "Built: target/TrapParty-1.0.0.jar"
