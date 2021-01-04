#!/usr/bin/env bash

cd tacontrol
./build-binary.sh

cd ..
./gradlew build
docker build --rm -t pegasis0/fetch-ta:latest .
