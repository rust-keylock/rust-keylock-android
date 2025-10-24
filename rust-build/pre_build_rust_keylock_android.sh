#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Base directory is $BASEDIR

# Set the home directories for cargo and rustup
mkdir -p tools/.cargo
mkdir -p tools/.rustup
CARGO_HOME=$BASEDIR/tools/.cargo
RUSTUP_HOME=$BASEDIR/tools/.rustup

# Install the latest rust stable and cargo
PATH=$CARGO_HOME/bin:$PATH

curl https://sh.rustup.rs -sSf > tools/rustup.sh
chmod +x tools/rustup.sh

CARGO_HOME=$BASEDIR/tools/.cargo RUSTUP_HOME=$BASEDIR/tools/.rustup sh tools/rustup.sh --no-modify-path -y

$CARGO_HOME/bin/rustup default stable
$CARGO_HOME/bin/rustup target add aarch64-linux-android
$CARGO_HOME/bin/rustup target add armv7-linux-androideabi

cd $BASEDIR

# Get the appropriate env variable in order to find the needed toolchain
if [ -n "${ANDROID_NDK}" ]; then
	echo "Found a pre-installed NDK in ${ANDROID_NDK}"
else
	# If the NDK does not exist, download it
	echo "Did not find a pre-installed NDK... Downloading one"
	curl -L https://dl.google.com/android/repository/android-ndk-r26d-linux.zip -O
	unzip android-ndk-r26d-linux.zip
	rm android-ndk-r26d-linux.zip
	ANDROID_NDK=`pwd`/android-ndk-r26d
	PATH=$PATH:${ANDROID_NDK}
fi

echo Using NDK: $ANDROID_NDK

# Prepare the env vars
. ./rust-build/setenv-android.sh

echo Android toolchain is in $ANDROID_TOOLCHAIN_DIR

# Go to the .cargo directory
cd $BASEDIR/rust
mkdir -p .cargo
cd .cargo
CURR_DIR=`pwd`
echo Entered directory $CURR_DIR

# Create a config file
cat > config.toml << EOF
[target]
 [target.armv7-linux-androideabi]
 linker = "${ANDROID_TOOLCHAIN_DIR}/bin/armv7a-linux-androideabi32-clang"
 [target.arm-linux-androideabi]
 linker = "${ANDROID_TOOLCHAIN_DIR}/bin/armv7a-linux-androideabi32-clang"
 [target.aarch64-linux-android]
 linker = "${ANDROID_TOOLCHAIN_DIR}/bin/aarch64-linux-android32-clang"
 [target.x86_64-linux-android]
 linker = "${ANDROID_TOOLCHAIN_DIR}/bin/x86_64-linux-android32-clang"
 [target.i686-linux-android]
 linker = "${ANDROID_TOOLCHAIN_DIR}/bin/i686-linux-android32-clang"
EOF

