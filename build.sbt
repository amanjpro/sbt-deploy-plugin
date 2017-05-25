lazy val root = Project(id="sbt-adgera-deploy", base=file(".")).settings(Seq(
    organization := "com.adgear.data",
    name := "sbt-adgear-deploy",
    version := "1.0.0",
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    publishMavenStyle := false,
    addSbtPlugin("com.eed3si9n" %% "sbt-assembly" % "0.14.4")
  ))

publishTo := {
  if(isSnapshot.value)
    Some("Artifactory Realm" at "https://adgear.jfrog.io/adgear/adgear-sbt-plugins-snapshots;build.timestamp=" + new java.util.Date().getTime)
  else
    Some("Artifactory Realm" at "https://adgear.jfrog.io/adgear/adgear-sbt-plugins-releases")
}
