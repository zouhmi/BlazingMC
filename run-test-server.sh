#!/usr/bin/env bash
set -euo pipefail

./gradlew :server-core:build
server_jar=$(find server-core/build/libs -maxdepth 1 -type f -name 'server-core-*.jar' ! -name '*-plain.jar' | sort | head -n 1)

if [[ -z "$server_jar" ]]; then
    printf 'Server JAR was not created.\n' >&2
    exit 1
fi

exec java -Xms1G -Xmx1G -XX:+UseG1GC -Dblazingmc.plugins="${BLAZINGMC_PLUGIN_DIR:-plugins}" -jar "$server_jar"
