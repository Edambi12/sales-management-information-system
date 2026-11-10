#!/usr/bin/env bash
# Builds the application with Maven and runs the Swing/JDBC integration test.
# Prerequisites: JDK 17+, Maven, a MySQL/MariaDB server with the demo database
# (database/schema.sql + database/sample_data.sql) and config/database.properties
# (or SOKTRAPA_DB_* environment variables / -Dsoktrapa.config).
set -euo pipefail
cd "$(dirname "$0")/.."

mvn -q -B package
OUT=target/it-classes
mkdir -p "$OUT"
javac -encoding UTF-8 -cp target/classes -d "$OUT" tests/integration/SwingIntegrationTest.java

CP="$OUT:target/classes:target/lib/*"
RUNNER=()
if [[ -z "${DISPLAY:-}" ]] && command -v xvfb-run >/dev/null; then RUNNER=(xvfb-run -a); fi
"${RUNNER[@]}" java ${SOKTRAPA_CONFIG:+-Dsoktrapa.config="$SOKTRAPA_CONFIG"} -cp "$CP" SwingIntegrationTest
