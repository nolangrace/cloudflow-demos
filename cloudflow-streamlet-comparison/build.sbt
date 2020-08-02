import sbt._
import sbt.Keys._

val AkkaVersion = "2.6.8"
val AkkaPersistenceCassandraVersion = "1.0.0"
val AkkaHttpVersion = "10.1.10"
val AkkaProjectionVersion = "0.3"

lazy val root =
  Project(id = "root", base = file("."))
    .enablePlugins(ScalafmtPlugin)
    .settings(
      name := "root",
      scalafmtOnCompile := true,
      skip in publish := true,
    )
    .withId("root")
    .settings(commonSettings)
    .aggregate(
      cloudflowStreamletComparison,
      resourceConsumption,
      akkaStreamlet,
      sparkStreamlet,
      datamodel
    )

lazy val cloudflowStreamletComparison = (project in file("./cloudflow-streaming-comparison"))
  .enablePlugins(CloudflowApplicationPlugin)
  .settings(
    commonSettings,
    name := "cloudflow-streaming-comparison",
    libraryDependencies ++= Seq(
      "org.scalatest"  %% "scalatest"       % "3.0.7"    % "test"
      )
  )

lazy val datamodel = (project in file("./datamodel"))
  .enablePlugins(CloudflowLibraryPlugin)

lazy val akkaStreamlet = (project in file("./akka-streamlet"))
  .enablePlugins(CloudflowAkkaPlugin)
  .settings(
    commonSettings,
    name := "akka-streamlet",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest"          %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel, resourceConsumption)

lazy val sparkStreamlet = (project in file("./spark-streamlet"))
  .enablePlugins(CloudflowSparkPlugin)
  .settings(
    commonSettings,
    Test / parallelExecution := false,
    Test / fork := true,
    libraryDependencies ++= Seq(
      "ch.qos.logback" %  "logback-classic" % "1.2.3",
      "org.scalatest"  %% "scalatest"       % "3.0.8"  % "test"
    )
  )
  .dependsOn(datamodel, resourceConsumption)

lazy val resourceConsumption = (project in file("./resource-consumption"))
  .settings(
    commonSettings,
    name := "resource-consumption",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.scalatest"          %% "scalatest"              % "3.0.8"    % "test"
    )
  )
  .dependsOn(datamodel)

lazy val commonSettings = Seq(
  scalaVersion := "2.12.11",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-Xlog-reflective-calls",
    "-Xlint",
    "-Ywarn-unused",
    "-Ywarn-unused-import",
    "-deprecation",
    "-feature",
    "-language:_",
    "-unchecked"
  ),

  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused", "-Ywarn-unused-import"),
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value

)
