#!/bin/bash

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
`curl https://sh.rustup.rs -sSf | CARGO_HOME=$BASEDIR/tools/.cargo RUSTUP_HOME=$BASEDIR/tools/.rustup sh -s -- -y`
PATH=$PATH:$CARGO_HOME/bin

rustup default stable
rustup target add arm-linux-androideabi

# Install xargo
cargo install xargo --force

# Create an Android toolchain
cd $BASEDIR
sh $ANDROID_NDK/build/tools/make-standalone-toolchain.sh --platform=android-16 --arch=arm --install-dir=android-toolchain
cd android-toolchain
ANDROID_TOOLCHAIN_DIR=`pwd`
echo Android toolchain set in $ANDROID_TOOLCHAIN_DIR

# Go to the .cargo directory
cd $BASEDIR/rust
mkdir .cargo
cd .cargo
CURR_DIR=`pwd`
echo Entered directory $CURR_DIR

#Create a config file
cat > config << EOF
[target]
[target.arm-linux-androideabi]
linker = "${ANDROID_TOOLCHAIN_DIR}/bin/arm-linux-androideabi-gcc"
EOF