#!/bin/bash
BASEDIR=$(dirname "$0"); echo Installing custom jar jna-min-4.3.0.jar in the local Maven... Entered directory ${BASEDIR}
mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=${BASEDIR}/libs/jna-min-4.3.0.jar -DgroupId=net.java.dev.jna -DartifactId=jna-min -Dversion=4.3.0 -Dpackaging=jar