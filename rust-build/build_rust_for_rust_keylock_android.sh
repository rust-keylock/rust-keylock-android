#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Entered Base directory $BASEDIR

ANDROID_RUST="$BASEDIR/rust"
ANDROID_RUST_KEYLOCK_LIB="$ANDROID_RUST/target/arm-linux-androideabi/release/librustkeylockandroid.so"
ANDROID_JAVA_NATIVE="$BASEDIR/java/libs/armeabi/"
cd $ANDROID_RUST
xargo build --target=arm-linux-androideabi --release

echo "Copying $ANDROID_RUST_KEYLOCK_LIB to $ANDROID_JAVA_NATIVE"
cp $ANDROID_RUST_KEYLOCK_LIB $ANDROID_JAVA_NATIVE

JNA_LIB="$BASEDIR/rust-build/libs/armeabi/libjnidispatch.so"
echo "Copying $JNA_LIB to $ANDROID_JAVA_NATIVE"
cp $JNA_LIB $ANDROID_JAVA_NATIVE

echo "Rust build for rust-keylock-android completed."