#!/usr/bin/env sh

./gradlew build --stacktrace
mkdir -p build/release
cp build/bin/tacontrol/releaseExecutable/tacontrol.kexe build/release/tacontrol
strip build/release/tacontrol

echo
echo "---------------------------------------------"
echo "The tacontrol binary is under build/release/."
