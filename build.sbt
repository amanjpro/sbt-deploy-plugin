lazy val root = Project(id="sbt-adgear-deploy", base=file(".")).settings(Seq(
    organization := "com.adgear.data",
    name := "sbt-adgear-deploy",
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

publishTo := {

  if(isSnapshot.value)
    Some("Artifactory Realm" at "https://adgear.jfrog.io/adgear/adgear-sbt-plugins-snapshots;build.timestamp=" + new java.util.Date().getTime)
  else
    Some("Artifactory Realm" at "https://adgear.jfrog.io/adgear/adgear-sbt-plugins-releases")
}

