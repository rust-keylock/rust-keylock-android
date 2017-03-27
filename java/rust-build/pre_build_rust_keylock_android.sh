#!/bin/bash
# Install the latest rust stable and cargo
`curl https://sh.rustup.rs -sSf | sh -s -- -y`
PATH=$PATH:/root/.cargo/bin

# Install xargo
cargo install xargo

BASEDIR=$(dirname "$0")
cd $BASEDIR/../../
BASEDIR=`pwd`
echo Base directory is $BASEDIR

# Get the directory of the toolchain
mkdir android-toolchain
cd android-toolchain
ANDROID_TOOLCHAIN_DIR=`pwd`
echo Android toolchain set in $ANDROID_TOOLCHAIN_DIR

# Go to the rust directory
cd $BASEDIR/rust
CURR_DIR=`pwd`
echo Entered directory $CURR_DIR

#Create a config file
cat > config << EOF
[target]
[target.arm-linux-androideabi]
linker = "${ANDROID_TOOLCHAIN_DIR}/bin/arm-linux-androideabi-gcc"
EOF

# Install the needed custom jar (JNA)
echo Installing custom jar jna-min-4.3.0.jar in the local Maven...
echo Entered directory ${BASEDIR}
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=${BASEDIR}/java/rust-build/libs/jna-min-4.3.0.jar -DgroupId=net.java.dev.jna -DartifactId=jna-min -Dversion=4.3.0 -Dpackaging=jar
