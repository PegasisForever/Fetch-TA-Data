#!/usr/bin/env bash

./gradlew build
docker build --rm -t pegasis0/fetch-ta:latest .
