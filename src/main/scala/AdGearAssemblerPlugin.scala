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
    lazy val distributionProjectName  = settingKey[String]("Name (or group id) of the distribution project")
    lazy val assemblyClassifier       = settingKey[String]("The classifier for assembled projects")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    distributionProjectName := name.value,
    targetDistributionDir := file("distribution") / "target",
    // set custom settings for assembly
    // for more detail please see the sbt-assembly project
    assemblyClassifier := "jar-with-dependencies",
    test in assembly := {},
    
    assemblyOutputPath in assembly := targetDistributionDir.value / s"${distributionProjectName.value}-${version.value}-dist" /
      s"${distributionProjectName.value}-${version.value}" / "lib" / s"${(assemblyJarName in assembly).value}",
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
    assemblyJarName in assembly := {
      s"${name.value}-${version.value}-${assemblyClassifier.value}.jar"
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
