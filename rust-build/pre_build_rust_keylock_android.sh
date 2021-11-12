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

cd $BASEDIR

# Get the appropriate env variable in order to find the needed toolchain
if [ -n "${ANDROID_NDK}" ]; then
	echo "Found a pre-installed NDK in ${ANDROID_NDK}"
else
	# If the NDK does not exist, download it
	echo "Did not find a pre-installed NDK... Downloading one"
	curl -L https://dl.google.com/android/repository/android-ndk-r22b-linux-x86_64.zip -O
	unzip android-ndk-r22b-linux-x86_64.zip
	rm android-ndk-r22b-linux-x86_64.zip
	ANDROID_NDK=`pwd`/android-ndk-r22b
	PATH=$PATH:${ANDROID_NDK}
fi

echo Using NDK: $ANDROID_NDK

# Prepare the env vars
. ./rust-build/setenv-android.sh

echo Android toolchain is in $ANDROID_TOOLCHAIN_DIR

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
# Choose for android-16
linker = "${ANDROID_TOOLCHAIN_DIR}/bin/armv7a-linux-androideabi16-clang"
EOF

# Get and build the openssl
cd $BASEDIR/tools

curl -O https://www.openssl.org/source/openssl-1.1.1l.tar.gz
tar xzf openssl-1.1.1l.tar.gz

export OPENSSL_SRC_DIR=$BASEDIR/tools/openssl-1.1.1l

cd $OPENSSL_SRC_DIR

echo Building openssl
./Configure ${architecture} -D__ANDROID_API__=$ANDROID_API --openssldir=$OPENSSL_SRC_DIR/build --prefix=$OPENSSL_SRC_DIR/build
make
make install

echo openssl build success
