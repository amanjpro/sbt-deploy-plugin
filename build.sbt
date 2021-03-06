lazy val commonSettings = Seq(
  version in ThisBuild := "2.4.0-SNAPSHOT",
  organization in ThisBuild := "me.amanj"
)

lazy val root = Project(id="sbt-deploy", base=file(".")).settings(
    commonSettings,
    name := "sbt-deploy",
    scalaVersion := "2.12.0",
    sbtPlugin := true,
    publishMavenStyle := true,
    crossSbtVersions := Vector("0.13.16", "1.0.0"),
    libraryDependencies += {
      val currentSbtVersion = (sbtBinaryVersion in pluginCrossBuild).value
      Defaults.sbtPluginExtra("com.eed3si9n" % "sbt-assembly" % "0.14.10", currentSbtVersion, scalaBinaryVersion.value)
    },
    licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
    publishMavenStyle := false,
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None
  )

