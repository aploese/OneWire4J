#!/bin/bash
#export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
mvn clean -P release compile deploy
