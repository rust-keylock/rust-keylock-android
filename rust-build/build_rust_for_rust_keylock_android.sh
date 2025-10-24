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
ANDROID_RUST_KEYLOCK_LIB_AARCH64="$ANDROID_RUST/target/aarch64-linux-android/release/librustkeylockandroid.so"
ANDROID_RUST_KEYLOCK_LIB_ARMV7="$ANDROID_RUST/target/armv7-linux-androideabi/release/librustkeylockandroid.so"
ANDROID_RUST_KEYLOCK_LIB_x86="$ANDROID_RUST/target/x86_64-linux-android/release/librustkeylockandroid.so"
ANDROID_JAVA_NATIVE_AARCH64="$BASEDIR/app/src/main/jniLibs/arm64-v8a/"
ANDROID_JAVA_NATIVE_ARMV7="$BASEDIR/app/src/main/jniLibs/armeabi-v7a/"
ANDROID_JAVA_NATIVE_x86="$BASEDIR/app/src/main/jniLibs/x86_64/"

mkdir -p $ANDROID_JAVA_NATIVE_AARCH64
mkdir -p $ANDROID_JAVA_NATIVE_ARMV7
mkdir -p $ANDROID_JAVA_NATIVE_x86

cd $ANDROID_RUST

# ANDROID_TOOLCHAIN_DIR should be already set by the pre_build_rust_keylock_android.sh
if [ -n "${ANDROID_TOOLCHAIN_DIR}" ]; then
	echo "Found a toolchain in ${ANDROID_TOOLCHAIN_DIR}"
else
  if [ -n "${ANDROID_NDK}" ]; then
	  ANDROID_TOOLCHAIN_DIR="$ANDROID_NDK/toolchains/llvm/prebuilt/linux-x86_64"
  else
    echo "********************* ERROR *********************"
    echo "       The env var ANDROID_NDK is not set"
    echo "*************************************************"
    exit 1
  fi
fi

echo "ANDROID_TOOLCHAIN_DIR: $ANDROID_TOOLCHAIN_DIR"
echo "CARGO_HOME: $CARGO_HOME"

export RANLIB=${ANDROID_TOOLCHAIN_DIR}/bin/llvm-ranlib
export AR=${ANDROID_TOOLCHAIN_DIR}/bin/llvm-ar

echo "********* BUILDING for armv7-linux-androideabi *********"
export CC=${ANDROID_TOOLCHAIN_DIR}/bin/armv7a-linux-androideabi32-clang
$CARGO_HOME/bin/cargo build --target=armv7-linux-androideabi --release

echo "********* BUILDING for aarch64-linux-android *********"
export CC=${ANDROID_TOOLCHAIN_DIR}/bin/aarch64-linux-android32-clang
$CARGO_HOME/bin/cargo build --target=aarch64-linux-android --release

echo "********* BUILDING for x86_64-linux-android *********"
export CC=${ANDROID_TOOLCHAIN_DIR}/bin/x86_64-linux-android32-clang
$CARGO_HOME/bin/cargo build --target=x86_64-linux-android --release

echo "Copying $ANDROID_RUST_KEYLOCK_LIB_AARCH64 to $ANDROID_JAVA_NATIVE_AARCH64"
cp $ANDROID_RUST_KEYLOCK_LIB_AARCH64  $ANDROID_JAVA_NATIVE_AARCH64
echo "Copying $ANDROID_RUST_KEYLOCK_LIB_ARMV7 to $ANDROID_JAVA_NATIVE_ARMV7"
cp $ANDROID_RUST_KEYLOCK_LIB_ARMV7  $ANDROID_JAVA_NATIVE_ARMV7
echo "Copying $ANDROID_RUST_KEYLOCK_LIB_x86 to $ANDROID_JAVA_NATIVE_x86"
cp $ANDROID_RUST_KEYLOCK_LIB_x86  $ANDROID_JAVA_NATIVE_x86

echo "Rust build for rust-keylock-android completed."

# Emulator commands

# emulator -list-avds
# emulator -avd $AVD_NAME
# adb install $REPO_BASE/rust-keylock-android/app/build/outputs/apk/debug/app-debug.apk
# adb logcat | grep rustkeylock