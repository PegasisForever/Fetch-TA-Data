#!/usr/bin/env sh

docker run --rm -v tacontrol_gradle_cache:/home/user/.gradle/ -v $(pwd)/:/compile pegasis0/tacontrol_build:latest ./build.sh
