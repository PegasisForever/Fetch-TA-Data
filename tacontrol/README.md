# TaControl

A command line utility to control a running fetch-ta server.

## Usage

```
tacontrol [-t <control url>] [args]...
```

## Build

```
./build-in-docker.sh
```

Will build a dynamically linked executable in a debian 10 docker and put it in `build/release/tacontrol`

## Create the Builder Docker Image

```
docker build -t pegasis0/tacontrol_build:latest -f build.Dockerfile .
```
