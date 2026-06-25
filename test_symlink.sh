#!/bin/bash
rm -rf /tmp/testdir /tmp/targetdir
mkdir -p /tmp/testdir
mkdir -p /tmp/targetdir
echo "target" > /tmp/targetdir/secret.txt
ln -s /tmp/targetdir /tmp/testdir/symlinkdir
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
./gradlew build
java -jar ./build/libs/app.jar /tmp/testdir
ls -la /tmp/targetdir
