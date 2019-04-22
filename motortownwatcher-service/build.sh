#!/bin/sh
gradle clean assemble
mkdir build/resources/main/lib
cp build/libs/*.jar build/resources/main/lib
cd build/resources/main
docker build -t registry.anjana.local:5001/motortownwatcher:1.0.0-SNAPSHOT-1 .
docker push registry.anjana.local:5001/motortownwatcher:1.0.0-SNAPSHOT-1

