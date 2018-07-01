inThisBuild(List(
  organization := "com.github.masseguillaume",
  homepage := Some(url("https://github.com/MasseGuillaume/api-diff")),
  licenses := Seq(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
  ),
  developers := List(
    Developer(
      "MasseGuillaume",
      "Guillaume Massé",
      "masgui@gmail.com",
      url("https://github.com/MasseGuillaume")
    )
  )
))

lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-unchecked"
  )
)

lazy val root = project.in(file(".")).aggregate(apiDiff)

lazy val coursierVersion = "1.1.0-M4"
lazy val scalametaVersion = "4.0.0-M4-42-083ac679" // https://github.com/scalameta/scalameta/pull/1646

lazy val apiDiff = project
  .in(file("api-diff"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "pprint" % "0.5.3",
      "org.scalameta" %% "metacp" % scalametaVersion,
      "org.scalameta" %% "metap" % scalametaVersion,
      "io.get-coursier" %% "coursier" % coursierVersion,
      "io.get-coursier" %% "coursier-cache" % coursierVersion
    )
  )

// test

lazy val apiDiffTest = project
  .in(file("test/test"))
  .settings(
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.3" % Test,
    buildInfoPackage := "fix",
    buildInfoKeys := Seq[BuildInfoKey](
      "oldApiClasspath" -> classDirectory.in(oldApi, Compile).value,
      "newApiClasspath" -> classDirectory.in(newApi, Compile).value
    ),
    test in Test := (test in Test).dependsOn(
      compile in (oldApi, Compile),
      compile in (newApi, Compile)
    ).value
  )
  .dependsOn(apiDiff)
  .enablePlugins(BuildInfoPlugin)


lazy val oldApi = project
  .in(file("test/old-api"))
  .settings(commonSettings)

lazy val newApi = project
  .in(file("test/new-api"))
  .settings(commonSettings)