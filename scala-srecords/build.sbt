val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-srecords",
    organization := "quito-lambda",
    version := "1.0.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "com.h2database" % "h2" % "2.1.214" % Test
    )
  )

