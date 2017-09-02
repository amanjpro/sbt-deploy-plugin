lazy val root = Project(id="sbt-deploy", base=file(".")).settings(Seq(
    organization := "me.amanj",
    name := "sbt-deploy",
    version := "2.1.0-SNAPSHOT",
    scalaVersion := "2.12.0",
    sbtPlugin := true,
    publishMavenStyle := true,
    crossSbtVersions := Vector("0.13.16", "1.0.0"),
    libraryDependencies += {
      val currentSbtVersion = (sbtBinaryVersion in pluginCrossBuild).value
      Defaults.sbtPluginExtra("com.eed3si9n" % "sbt-assembly" % "0.14.5", currentSbtVersion, scalaBinaryVersion.value)
    }
  ))
