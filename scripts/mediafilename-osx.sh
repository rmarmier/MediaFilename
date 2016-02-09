#!/usr/bin/env bash

# This script is meant to run MediaFilename on OSX.

# Set the Java home
export JAVA_HOME=`/usr/libexec/java_home`

java -jar mediafilename-1.0.jar $@

