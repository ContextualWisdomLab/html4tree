#!/bin/bash
mkdir -p test_dir
for i in {1..5000}; do
  touch test_dir/file_$i.txt
done
time export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64 && ./gradlew run --args="test_dir" > /dev/null
