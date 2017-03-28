#!/bin/bash

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Base directory is $BASEDIR

# Install the latest rust stable and cargo
`curl https://sh.rustup.rs -sSf | sh -s -- -y`
PATH=$PATH:$HOME/.cargo/bin

# Install xargo
cargo install xargo --force

# Get the directory of the toolchain
mkdir android-toolchain
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