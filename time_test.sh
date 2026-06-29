#!/bin/bash
mkdir -p test_dir
for i in {1..5000}; do touch test_dir/file_$i.txt; done
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
./gradlew build
time java -jar build/libs/app.jar test_dir
rm -rf test_dir
