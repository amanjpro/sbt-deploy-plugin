package com.adgear.sbt

import sbt._
import sbt.Keys._


object AdGearDistributionPlugin extends AutoPlugin {
  override def requires = AdGearShellCheckPlugin
  override def trigger  = noTrigger

  object autoImport {
    lazy val libDestDirName       = settingKey[String]("Name of the lib directory in the tarball")
    lazy val binDestDirName       = settingKey[String]("Name of the bin directory in the tarball")
    lazy val confDestDirName      = settingKey[String]("Name of the conf directory in the tarball")
    lazy val binSrcDir            = settingKey[File]("Path to the directory where launcher scripts live")
    lazy val confSrcDir           = settingKey[File]("Path to the directory where the configurations live")
    lazy val targetDir            = settingKey[File]("Path to the directory where the tarball should end")
    lazy val enableShellCheck    = settingKey[Boolean]("A flag to enable and disable shellcheck")
  }


  import autoImport._

  override lazy val projectSettings = Seq(
    libDestDirName := "lib",
    binDestDirName := "bin",
    confDestDirName := "conf",
    binSrcDir := (sourceDirectory in Compile).value / "scripts",
    confSrcDir := (resourceDirectory in Compile).value / "conf",
    targetDir := (target in Compile).value,
    enableShellCheck := true,
    exportJars := true,
    // When publishing (either to local repo, or public), make sure to publish the
    // tarball is published too
    packagedArtifacts in publish := {
      val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publish).value
      val tarball = targetDir.value / s"${name.value}-${version.value}.tar.gz"
      artifacts + (Artifact(name.value, "dist", "tar.gz", "dist") -> tarball)
    },
    packagedArtifacts in publishLocal := {
      val artifacts: Map[sbt.Artifact, java.io.File] = (packagedArtifacts in publishLocal).value
      val tarball = targetDir.value / s"${name.value}-${version.value}.tar.gz"
      artifacts + (Artifact(name.value, "dist", "tar.gz", "dist") -> tarball)
    },
    // generate archive after packaging
    (packageBin in Compile) := {
      val pkg = (packageBin in Compile).value

      val nestedDir = s"${name.value}-${version.value}-dist/${name.value}-${version.value}"
      val binSrc = binSrcDir.value.getAbsolutePath
      val confSrc = confSrcDir.value.getAbsolutePath

      val targetPath = targetDir.value.getAbsolutePath
      val binDest = (targetDir.value / nestedDir / binDestDirName.value).getAbsolutePath
      val confDest = (targetDir.value / nestedDir / confDestDirName.value).getAbsolutePath

      // We prefer this verbose way over the simple string2process to use
      // Scala's builtin facility to escape special chars
      List("cp", "-R", binSrc, binDest).#&&(List("chmod", "-R", "a+x", binDest)).!

      List("cp", "-R", confSrc, confDest).#||(List("mkdir", "-p", confDest)).!

      List("tar", "-C", targetPath, "-c", "-z", "-f", s"$targetPath/${name.value}-${version.value}.tar.gz", nestedDir).!

      pkg
    },
    // when testing, run shellcheck on the scripts in {{{binSrcDir.value}}} directory
    test in Test := {
      if(enableShellCheck.value) {
        Def.taskDyn[Unit] {
          val args = binSrcDir.value
            .listFiles
            .filter(!_.getName.startsWith("."))
            .map(f => "\"\"\"" + f.getAbsolutePath + "\"\"\"")// properly quote the paths
//            .map(f => f.getAbsolutePath)// properly quote the paths
            // Arguments should start with a whitespace in the input task universe
            .mkString(" ", " ", "")

          AdGearShellCheckPlugin.autoImport.shellCheck.toTask(args)
        }.value
      }
      (test in Test).value
    }
  )
}
