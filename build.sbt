name := "google-cloud-logging"

organization := "com.quadstingray"

//crossPaths := false

scalaVersion := crossScalaVersions.value.last

crossScalaVersions := List("2.12.14", "2.13.6")

scalacOptions := Seq("-unchecked", "-deprecation", "-Ywarn-unused", "-Yrangepos")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Provided

libraryDependencies ++= Seq(
  "com.google.cloud" % "google-cloud-logging" % "2.3.1"
)

// Tests
libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "4.12.2" % Test,
  "junit" % "junit" % "4.13.2" % Test
)

homepage := Some(url("https://quadstingray.github.io/google-cloud-logging/"))

scmInfo := Some(ScmInfo(url("https://github.com/QuadStingray/google-cloud-logging"), "https://github.com/QuadStingray/google-cloud-logging.git"))

developers := List(Developer("QuadStingray", "QuadStingray", "github@quadstingray.com", url("https://github.com/QuadStingray")))

licenses += ("Apache-2.0", url("https://github.com/QuadStingray/google-cloud-logging/blob/master/LICENSE"))

resolvers += Resolver.jcenterRepo

resolvers += Resolver.sonatypeRepo("releases")

resolvers += Resolver.jcenterRepo

bintrayReleaseOnPublish in ThisBuild := true

import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies, // : ReleaseStep
  inquireVersions, // : ReleaseStep
  setReleaseVersion, // : ReleaseStep
  commitReleaseVersion, // : ReleaseStep, performs the initial git checks
  tagRelease, // : ReleaseStep
  publishArtifacts,
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)

