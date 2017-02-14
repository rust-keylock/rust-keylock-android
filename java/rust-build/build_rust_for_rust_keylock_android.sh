#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/../..
CURR_DIR=`pwd`
echo Entered directory $CURR_DIR

ANDROID_CONTAINER_RUST="$CURR_DIR/rust"
ANDROID_CONTAINER_RUST_LIB="$ANDROID_CONTAINER_RUST/target/arm-linux-androideabi/release/librustkeylockandroid.so"
ANDROID_CONTAINER_JAVA_NATIVE="$CURR_DIR/java/libs/armeabi/"
cd $ANDROID_CONTAINER_RUST
xargo build --target=arm-linux-androideabi --release

echo "Copying $ANDROID_CONTAINER_RUST_LIB to $ANDROID_CONTAINER_JAVA_NATIVE"
cp $ANDROID_CONTAINER_RUST_LIB $ANDROID_CONTAINER_JAVA_NATIVE
echo "Rust builds for rust-keylock-android completed."