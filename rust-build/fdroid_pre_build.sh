#!/bin/bash

set -e

BASEDIR=$(dirname "$0")
cd $BASEDIR/../
BASEDIR=`pwd`
echo Base directory is $BASEDIR

# Create an Android toolchain
cd $BASEDIR

ANDROID_NDK_HOME=${ANDROID_NDK}

echo Installing the Android toolchain using NDK: $ANDROID_NDK

$ANDROID_NDK/build/tools/make-standalone-toolchain.sh --platform=android-16 --arch=arm --install-dir=android-toolchain  > /dev/null

cd android-toolchain
ANDROID_TOOLCHAIN_DIR=`pwd`

echo Android toolchain installed in $ANDROID_TOOLCHAIN_DIR

# Go to the .cargo directory
cd $HOME
mkdir -p .cargo
cd .cargo
CURR_DIR=`pwd`
echo Entered directory $CURR_DIR

# Create a config file
cat > config << EOF
[target]
[target.arm-linux-androideabi]
linker = "${ANDROID_TOOLCHAIN_DIR}/bin/arm-linux-androideabi-gcc"
EOF

# Get and build the openssl

# Export the ANDROID_TOOLCHAIN variable to be used for building the openssl
export ANDROID_TOOLCHAIN=$ANDROID_TOOLCHAIN_DIR/bin
export ANDROID_NDK_ROOT=${ANDROID_NDK}

mkdir $BASEDIR/tools
cd $BASEDIR/tools

curl -O https://www.openssl.org/source/openssl-1.1.1g.tar.gz
tar xzf openssl-1.1.1g.tar.gz

export OPENSSL_SRC_DIR=$BASEDIR/tools/openssl-1.1.1g

# Delete the mandroid flag as clang does not recognize it
sed -i 's/-mandroid //g' ${OPENSSL_SRC_DIR}/Configurations/10-main.conf

. $BASEDIR/rust-build/setenv-android.sh

cd $OPENSSL_SRC_DIR

echo Building openssl
./Configure android-armeabi shared no-ssl3 no-comp no-hw no-asm --openssldir=$OPENSSL_SRC_DIR/build --prefix=$OPENSSL_SRC_DIR/build
make all
make install CC=$ANDROID_TOOLCHAIN/arm-linux-androideabi-clang RANLIB=$ANDROID_TOOLCHAIN/arm-linux-androideabi-ranlib

echo openssl build success
