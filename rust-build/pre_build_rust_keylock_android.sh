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

# Install xargo
$CARGO_HOME/bin/cargo install xargo --force --root $CARGO_HOME

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

sh $ANDROID_NDK/build/tools/make-standalone-toolchain.sh --platform=android-16 --arch=arm --install-dir=android-toolchain
cd android-toolchain
ANDROID_TOOLCHAIN_DIR=`pwd`
echo Android toolchain set in $ANDROID_TOOLCHAIN_DIR

# Install the jar containing the jna native library for Android (taken from https://github.com/java-native-access/jna/tree/master/lib/native)
echo "Installing jar android-arm.jar in the local Maven... This jar is not provided in the Maven Central, rather by the project's GitHub repo."
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=${BASEDIR}/rust-build/libs/android-arm.jar -DgroupId=net.java.dev.jna -DartifactId=android-arm -Dversion=4.4.0 -Dpackaging=jar

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