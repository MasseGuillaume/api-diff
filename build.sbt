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

lazy val root = project.in(file(".")).aggregate(apiDiff)

lazy val apiDiff = project
  .in(file("api-diff"))
  .settings(
    scalaVersion := "2.12.6",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-unchecked"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" %% "metacp" % "4.0.0-M4",
      "io.get-coursier" %% "coursier" % "1.1.0-M4",
      "io.get-coursier" %% "coursier-cache" % "1.1.0-M4"
    )
  )
