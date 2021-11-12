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

OPENSSL_BUILD_DIR=$BASEDIR/tools/openssl-1.1.1l/build

echo "ANDROID_TOOLCHAIN_DIR: $ANDROID_TOOLCHAIN_DIR"
echo "OPENSSL_BUILD_DIR: $OPENSSL_BUILD_DIR"
echo "CARGO_HOME: $CARGO_HOME"

CC_arm_linux_androideabi="${ANDROID_TOOLCHAIN_DIR}/bin/armv7a-linux-androideabi16-clang" AR_arm_linux_androideabi="${ANDROID_TOOLCHAIN_DIR}/bin/llvm-ar" ARM_LINUX_ANDROIDEABI_OPENSSL_DIR=${OPENSSL_BUILD_DIR} ARM_LINUX_ANDROIDEABI_OPENSSL_LIB_DIR=${OPENSSL_BUILD_DIR}/lib ARM_LINUX_ANDROIDEABI_OPENSSL_INCLUDE_DIR=${OPENSSL_BUILD_DIR}/include OPENSSL_STATIC=true $CARGO_HOME/bin/cargo build --target=arm-linux-androideabi --release

echo "Copying $ANDROID_RUST_KEYLOCK_LIB to $ANDROID_JAVA_NATIVE"
cp $ANDROID_RUST_KEYLOCK_LIB $ANDROID_JAVA_NATIVE

echo "Rust build for rust-keylock-android completed."
