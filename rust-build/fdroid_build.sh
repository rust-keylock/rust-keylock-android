#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Base directory is $BASEDIR

ANDROID_RUST="$BASEDIR/rust"
ANDROID_RUST_KEYLOCK_LIB="$ANDROID_RUST/target/arm-linux-androideabi/release/librustkeylockandroid.so"
ANDROID_JAVA_NATIVE="$BASEDIR/java/libs/armeabi/"

mkdir -p $ANDROID_JAVA_NATIVE
cd $ANDROID_RUST

ANDROID_TOOLCHAIN_DIR=$BASEDIR/android-toolchain
OPENSSL_BUILD_DIR=$BASEDIR/tools/openssl-1.1.0g/build

echo "ANDROID_TOOLCHAIN_DIR: $ANDROID_TOOLCHAIN_DIR"
echo "OPENSSL_BUILD_DIR: $OPENSSL_BUILD_DIR"
echo "CARGO_HOME: $CARGO_HOME"

CC_arm_linux_androideabi="${ANDROID_TOOLCHAIN_DIR}/bin/arm-linux-androideabi-gcc" AR_arm_linux_androideabi="${ANDROID_TOOLCHAIN_DIR}/bin/arm-linux-androideabi-ar" OPENSSL_DIR=${OPENSSL_BUILD_DIR} OPENSSL_LIB_DIR=${OPENSSL_BUILD_DIR}/lib OPENSSL_INCLUDE_DIR=${OPENSSL_BUILD_DIR}/include OPENSSL_STATIC=true $CARGO_HOME/bin/cargo build --target=arm-linux-androideabi --release

echo "Copying $ANDROID_RUST_KEYLOCK_LIB to $ANDROID_JAVA_NATIVE"
cp $ANDROID_RUST_KEYLOCK_LIB $ANDROID_JAVA_NATIVE

echo "Rust build for rust-keylock-android completed."