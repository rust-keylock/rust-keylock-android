#!/bin/bash

set -e

export ANDROID_NDK_HOME=${ANDROID_NDK}
export ANDROID_NDK_ROOT=${ANDROID_NDK}
export ANDROID_TOOLCHAIN_DIR="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64"
export ANDROID_TOOLCHAIN="$ANDROID_TOOLCHAIN_DIR/bin"
# Set compiler clang, instead of gcc by default
export CC=clang
# Add toolchains bin directory to PATH
export PATH=$ANDROID_TOOLCHAIN:$PATH
# Set the Android API levels
export ANDROID_API=16
# Set the target architecture
# Can be android-arm, android-arm64, android-x86, android-x86 etc
export architecture=android-arm
echo "============================ ENV SET ============================"
echo "ANDROID_NDK: $ANDROID_NDK"
echo "ANDROID_TOOLCHAIN_DIR: $ANDROID_TOOLCHAIN_DIR"
echo "ANDROID_TOOLCHAIN: $ANDROID_TOOLCHAIN"
echo "CC: $CC"
echo "PATH: $PATH"
echo "ANDROID_API: $ANDROID_API"
echo "architecture: $architecture"
echo "================================================================="
