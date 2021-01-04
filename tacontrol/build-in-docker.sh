#!/usr/bin/env sh

docker run --rm -v tacontrol_cache:/home/user/ -v $(pwd)/:/compile pegasis0/tacontrol_build:latest ./build.sh
