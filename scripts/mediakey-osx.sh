#!/usr/bin/env bash

# This script is meant to run MediaKey on OSX.

# Set the Java home
export JAVA_HOME=`/usr/libexec/java_home`

java -jar mediakey-1.0.jar $@

