# google-cloud-logging
This project is an [Logback-Appender](https://logback.qos.ch/) for [Google Cloud Logging](https://cloud.google.com/logging/docs) and is inspired by the Google's original [Logback-Appender](https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/logging/logback).

@@@ warning
Package Structure in 1.0.0 changed.
Migration: replace "com.quadstingray.google.cloud.logging.logback.LoggingAppender" by "com.quadstingray.logging.logback.google.cloud.LoggingAppender"
@@@

## Build Informations
[![Build Status](https://travis-ci.org/QuadStingray/google-cloud-logging.svg?branch=master)](https://travis-ci.org/QuadStingray/google-cloud-logging)
[ ![Download from Bintray](https://api.bintray.com/packages/quadstingray/maven/google-cloud-logging/images/download.svg) ](https://bintray.com/quadstingray/maven/google-cloud-logging/_latestVersion)

## maven
google-cloud-logging is deployed on bintray (jcenter).

## SBT
@@@ vars
@@dependency[sbt,Maven,Gradle] {
  group="com.quadstingray"
  artifact="google-cloud-logging"
  version="$project.version$"
}
@@@

## Licence
[Apache 2 License.](https://github.com/QuadStingray/google-cloud-logging/blob/master/LICENSE)

## Todos:
- Tests
- Documentation

@@@ index
* [Enabling the plugin](samples/index.md)
@@@