#!/usr/bin/env bash

mvn package
docker build --rm -t pegasis0/fetch-ta:latest .
