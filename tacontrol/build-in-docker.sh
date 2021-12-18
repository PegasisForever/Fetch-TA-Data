#!/usr/bin/env sh

docker run --rm -t -v tacontrol_cache:/home/user/ -v $(pwd)/:/compile pegasis0/tacontrol_build:latest ./build.sh
