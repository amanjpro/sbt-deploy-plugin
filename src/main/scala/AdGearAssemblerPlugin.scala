package com.adgear.sbt

import sbt._
import sbt.Keys._
import sbtassembly.AssemblyKeys._
import sbtassembly.{AssemblyPlugin, PathList, Assembly, MergeStrategy}

object AdGearAssemblerPlugin extends AutoPlugin {

  override def requires = AssemblyPlugin
  override def trigger  = noTrigger

  object autoImport {
    lazy val targetDistributionDir    = settingKey[File]("Path to the target directory in distribution project")
    lazy val prepareForTarball        = settingKey[Boolean](
      """|A flag to specify if the jar should end directly in the
         |target dir or be prepared for inclusion in the tarball,
         |i.e. should end in the lib directory as accustomed by AdGear""".stripMargin)
    lazy val distributedProjectName   = settingKey[String]("Name (or group id) of the distributed project")
    lazy val assemblyClassifier       = settingKey[String]("The classifier for assembled projects")
    lazy val jarName                  = settingKey[String]("The base name of both fat and normal jars")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    distributedProjectName := name.value,
    prepareForTarball := true,
    targetDistributionDir := file("distribution") / "target",
    // set custom settings for assembly
    // for more detail please see the sbt-assembly project
    assemblyClassifier := "jar-with-dependencies",
    test in assembly := {},
    assemblyOutputPath in assembly := {
      val prepareForTarballValue = prepareForTarball.value
      val targetDistributionDirValue = targetDistributionDir.value
      val distributedProjectNameValue = distributedProjectName.value
      val versionValue = version.value
      val assemblyJarNameValue = (assemblyJarName in assembly).value

      if(prepareForTarballValue) {
        targetDistributionDirValue / s"$distributedProjectNameValue-$versionValue-dist" /
          s"$distributedProjectNameValue-$versionValue" / "lib" / assemblyJarNameValue
      } else targetDistributionDirValue / assemblyJarNameValue
    },
    assemblyMergeStrategy in assembly := {
      case PathList(path @ _*)
        if path.exists(x => Assembly.isConfigFile(x)) ||
           path.exists(x => Assembly.isSystemJunkFile(x)) ||
           path.exists(x => x.toUpperCase.startsWith("README")) ||
           path.exists(x => x.toUpperCase.startsWith("LICENSE")) ||
           path.exists(x => x.toUpperCase == "META-INF") ||
           path.exists(x => x.toUpperCase.startsWith("NOTICE")) =>
        MergeStrategy.discard
      case x                               =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    exportJars in assembly := true,
    jarName := s"${name.value}-${version.value}",
    assemblyJarName in assembly := {
      s"${jarName.value}-${assemblyClassifier.value}.jar"
    },
    artifactName in Compile := { (_, _, artifact: Artifact) =>
      s"${jarName.value}.${artifact.extension}"
    },
    // When publishing (either to local repo, or public), make sure to publish the
    // fat jar too
    packagedArtifacts in publish := {
      val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publish).value
      val fatJar = (assemblyOutputPath in assembly).value
      val name = fatJar.getName.replaceFirst(s"-${version.value}-${assemblyClassifier.value}.jar$$", "")
      artifacts +
          (Artifact(name, "jar", "jar", assemblyClassifier.value) -> fatJar)
    },
    packagedArtifacts in publishLocal := {
      val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
      val fatJar = (assemblyOutputPath in assembly).value
      val name = fatJar.getName.replaceFirst(s"-${version.value}-${assemblyClassifier.value}.jar$$", "")
      artifacts +
          (Artifact(name, "jar", "jar", assemblyClassifier.value) -> fatJar)
    },
    // make package trigger assembly
    (packageBin in Compile) := ((packageBin in Compile) dependsOn (assembly)).value
  )
}
