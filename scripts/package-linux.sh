#!/usr/bin/env bash
# Builds CodeGauge and packages it as a native Linux app-image using jpackage
# (bundled with the JDK — no extra download needed).
#
# Produces a self-contained folder with a bundled JVM: target/dist/CodeGauge/
# Run it directly via target/dist/CodeGauge/bin/CodeGauge
#
# For a real installable .deb, add --type deb (requires dpkg-deb installed)
# or --type rpm (requires rpmbuild installed).
set -euo pipefail

cd "$(dirname "$0")/.."

echo "Building shaded jar..."
mvn -q clean package

echo "Preparing jpackage input..."
rm -rf target/jpackage-input
mkdir -p target/jpackage-input
cp target/codegauge.jar target/jpackage-input/

echo "Running jpackage..."
jpackage \
    --type app-image \
    --input target/jpackage-input \
    --dest target/dist \
    --name CodeGauge \
    --app-version 1.0.0 \
    --vendor "Prakash Mishra" \
    --main-jar codegauge.jar \
    --description "Repository quality analyzer"

echo "Done. Run it with: target/dist/CodeGauge/bin/CodeGauge analyze <path>"