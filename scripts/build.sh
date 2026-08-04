#!/usr/bin/env bash
set -uo pipefail

cd /root/home/forks/EMI-Compendium || exit 1

JAVA25=/usr/lib/jvm/java-25-openjdk-amd64
LOG=/root/home/forks/emi-compendium-26.1.2-build.log

export JAVA_HOME="$JAVA25"
export PATH="$JAVA25/bin:$PATH"

./gradlew clean build --no-daemon --no-build-cache --no-configuration-cache --stacktrace --console=plain |& tee "$LOG"

rc=${PIPESTATUS[0]}

printf '\n[BUILD EXIT]\n%s\n' "$rc"
printf '[BUILD LOG]\n%s\n' "$LOG"

exit "$rc"
