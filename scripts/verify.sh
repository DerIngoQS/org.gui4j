#!/usr/bin/env bash
set -euo pipefail

echo "== Tooling =="
java -version || true
mvn -version || true
ant -version || true

if [[ -f pom.xml ]]; then
  echo "== Maven verify =="
  mvn -q -DskipTests=false test
elif [[ -f build.xml ]]; then
  echo "== Ant build =="
  ant -q || ant
else
  echo "No pom.xml or build.xml found."
  exit 1
fi
