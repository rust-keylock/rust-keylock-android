#!/bin/bash

set -e

sh rust-build/pre_build_rust_keylock_android.sh
sh rust-build/build_rust_for_rust_keylock_android.sh
mvn -f java/pom.xml clean install