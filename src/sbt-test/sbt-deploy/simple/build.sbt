val Organization = "sbt.test"
val ProjectName = "simple"
val projectScalaVersion = "2.12.2"
val Version = "0.0.1-SNAPSHOT"

organization := Organization

name := ProjectName

scalaVersion := projectScalaVersion

version := Version

crossPaths := false

publishMavenStyle := true

def project(baseDir: String, plugin: Option[AutoPlugin] = None): Project = {
  val projectId = s"$ProjectName-$baseDir"

  val prj = Project(id = projectId, base = file(baseDir))
    .settings(Seq(name := projectId, version := Version,
    organization := Organization, scalaVersion := projectScalaVersion,
    exportJars in Compile := false
    ))

  plugin.map {
    case p@DistributionPlugin => prj.enablePlugins(p).settings(projectName := ProjectName)
    case p@AssemblerPlugin    => prj.enablePlugins(p).settings(Seq(publishMavenStyle := true,
        distributedProjectName := ProjectName))
  }.getOrElse(prj.settings(packagedArtifacts := Map.empty))
}


lazy val main = project("main", Some(AssemblerPlugin))

lazy val core = project("core", Some(AssemblerPlugin))

lazy val distribution = project("distribution", Some(DistributionPlugin)).settings(
  (packageBin in Compile) := ((packageBin in Compile) dependsOn (
    packageBin in Compile in core,
    packageBin in Compile in main)).value
)

