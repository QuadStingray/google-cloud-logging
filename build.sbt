name := "google-cloud-logging"

organization := "com.quadstingray"

crossPaths := false

scalaVersion := "2.12.8"

crossScalaVersions := List("2.12.8", "2.13.0")

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Provided

libraryDependencies ++= Seq(
  "com.google.cloud" % "google-cloud-logging" % "1.78.0"
)

// Tests
libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-core" % "4.5.1" % Test,
  "junit" % "junit" % "4.12" % Test
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
  setNextVersion, // : ReleaseStep
  commitNextVersion, // : ReleaseStep
  pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
)
