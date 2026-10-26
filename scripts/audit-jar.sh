#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

JAR="$(
    find build/libs \
        -maxdepth 1 \
        -type f \
        -name '*.jar' \
        ! -name '*sources*' \
        -print |
    head -n 1
)"

test -f "$JAR"

printf '\n[JAR]\n'
ls -lh "$JAR"
sha256sum "$JAR"

printf '\n[METADATA]\n'
jar tf "$JAR" |
grep -Fx 'META-INF/neoforge.mods.toml'

printf '\n[MOD CLASS]\n'
jar tf "$JAR" |
grep -Fx \
'ru/mehtm/emicompendium/EmiCompendium.class'

printf '\n[EMI PLUGIN]\n'
jar tf "$JAR" |
grep -Fx \
'ru/mehtm/emicompendium/EmiCompendiumPlugin.class'

printf '\n[JEI/JEMI AUDIT]\n'

if jar tf "$JAR" |
   grep -Ei 'mezz/jei|jemi'
then
    echo "Обнаружена запрещённая JEI/JEMI зависимость"
    exit 1
else
    echo "JEI и JEMI отсутствуют"
fi
