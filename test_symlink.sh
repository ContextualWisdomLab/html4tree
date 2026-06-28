#!/bin/bash
set -e
rm -rf /tmp/testdir /tmp/targetdir
mkdir -p /tmp/testdir
mkdir -p /tmp/targetdir
echo "target" > /tmp/targetdir/secret.txt
ln -s /tmp/targetdir /tmp/testdir/symlinkdir
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
./gradlew build
java -jar ./build/libs/html4tree.jar /tmp/testdir
ls -la /tmp/targetdir
if [ -f /tmp/targetdir/index.html ]; then
    echo "FAIL: symlink vulnerability regressed — index.html was written to /tmp/targetdir"
    exit 1
fi
echo "PASS: /tmp/targetdir/index.html was not created"