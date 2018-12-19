#!/bin/bash

set -e

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
PATH=$CARGO_HOME/bin:$PATH

curl https://sh.rustup.rs -sSf > tools/rustup.sh
chmod +x tools/rustup.sh

CARGO_HOME=$BASEDIR/tools/.cargo RUSTUP_HOME=$BASEDIR/tools/.rustup sh tools/rustup.sh --no-modify-path -y

$CARGO_HOME/bin/rustup default stable
$CARGO_HOME/bin/rustup target add arm-linux-androideabi

# Create an Android toolchain
cd $BASEDIR

# Get the appropriate env variable in order to create the needed toolchain

if [ -n "${ANDROID_NDK}" ]; then
	echo "Found a pre-installed NDK in ${ANDROID_NDK}"
else
	# If the NDK does not exist, download it
	echo "Did not find a pre-installed NDK... Downloading one"
	curl -L http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O
	chmod u+x android-ndk-r10e-linux-x86_64.bin
	./android-ndk-r10e-linux-x86_64.bin > /dev/null
	rm android-ndk-r10e-linux-x86_64.bin
	ANDROID_NDK=`pwd`/android-ndk-r10e
	PATH=$PATH:${ANDROID_NDK}
fi

ANDROID_NDK_HOME=${ANDROID_NDK}

echo Installing the Android toolchain using NDK: $ANDROID_NDK

$ANDROID_NDK/build/tools/make-standalone-toolchain.sh --platform=android-16 --arch=arm --install-dir=android-toolchain  > /dev/null

cd android-toolchain
ANDROID_TOOLCHAIN_DIR=`pwd`

echo Android toolchain installed in $ANDROID_TOOLCHAIN_DIR

# Go to the .cargo directory
cd $BASEDIR/rust
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

cd $BASEDIR/tools

curl -O https://www.openssl.org/source/openssl-1.1.0g.tar.gz
tar xzf openssl-1.1.0g.tar.gz

export OPENSSL_SRC_DIR=$BASEDIR/tools/openssl-1.1.0g

# Delete the mandroid flag as clang does not recognize it
sed -i 's/-mandroid //g' ${OPENSSL_SRC_DIR}/Configurations/10-main.conf

. ../rust-build/setenv-android.sh

cd $OPENSSL_SRC_DIR

echo Building openssl
#./config android shared no-ssl3 no-comp no-hw no-engine --openssldir=$OPENSSL_SRC_DIR/build --prefix=$OPENSSL_SRC_DIR/build
./Configure android-armeabi shared no-ssl3 no-comp no-hw no-asm --openssldir=$OPENSSL_SRC_DIR/build --prefix=$OPENSSL_SRC_DIR/build
make all > /dev/null
make install CC=$ANDROID_TOOLCHAIN/arm-linux-androideabi-clang RANLIB=$ANDROID_TOOLCHAIN/arm-linux-androideabi-ranlib > /dev/null

echo openssl build success
