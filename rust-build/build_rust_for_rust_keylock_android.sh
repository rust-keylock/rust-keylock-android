#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

# Add cargo to the path
CARGO_HOME=$BASEDIR/tools/.cargo
RUSTUP_HOME=$BASEDIR/tools/.rustup
PATH=$CARGO_HOME/bin:$PATH

ANDROID_RUST="$BASEDIR/rust"
ANDROID_RUST_KEYLOCK_LIB="$ANDROID_RUST/target/arm-linux-androideabi/release/librustkeylockandroid.so"
ANDROID_JAVA_NATIVE="$BASEDIR/java/libs/armeabi/"

mkdir -p $ANDROID_JAVA_NATIVE
cd $ANDROID_RUST

$CARGO_HOME/bin/xargo build --target=arm-linux-androideabi --release

echo "Copying $ANDROID_RUST_KEYLOCK_LIB to $ANDROID_JAVA_NATIVE"
cp $ANDROID_RUST_KEYLOCK_LIB $ANDROID_JAVA_NATIVE

echo "Rust build for rust-keylock-android completed."