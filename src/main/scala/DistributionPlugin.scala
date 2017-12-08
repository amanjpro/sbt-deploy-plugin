package me.amanj.sbt

import sbt._
import sbt.Keys._


object DistributionPlugin extends AutoPlugin {
  override def requires = ShellCheckPlugin
  override def trigger  = noTrigger

  object autoImport {
    lazy val libDestDirName       = settingKey[String]("Name of the lib directory in the tarball")
    lazy val binDestDirName       = settingKey[String]("Name of the bin directory in the tarball")
    lazy val confDestDirName      = settingKey[String]("Name of the conf directory in the tarball")
    lazy val binSrcDir            = settingKey[File]("Path to the directory where launcher scripts live")
    lazy val confSrcDir           = settingKey[File]("Path to the directory where the configurations live")
    lazy val targetDir            = settingKey[File]("Path to the directory where the tarball should end")
    lazy val projectName          = settingKey[String]("Name (or group id) of the distributed project")
    lazy val enableShellCheck     = settingKey[Boolean]("A flag to enable and disable shellcheck")
  }


  import autoImport._

  override lazy val projectSettings = Seq(
    libDestDirName := "lib",
    binDestDirName := "bin",
    confDestDirName := "conf",
    projectName := name.value,
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

      val binSrc = binSrcDir.value.getAbsolutePath
      val confSrc = confSrcDir.value.getAbsolutePath

      val distDir = s"${projectName.value}-${version.value}"
      val rootDir = targetDir.value / s"${projectName.value}-${version.value}-dist"
      val binDest = (rootDir / distDir / binDestDirName.value).getAbsolutePath
      val confDest = (rootDir / distDir / confDestDirName.value).getAbsolutePath

      // We prefer this verbose way over the simple string2process to use
      // Scala's builtin facility to escape special chars
      Seq("mkdir", "-p", binDest).#&&(Seq("/bin/sh", "-c", s"cp -R $binSrc/* $binDest/")).#&&(Seq("chmod", "-R", "a+x", binDest)).!

      Seq("mkdir", "-p", confDest).#&&(Seq("/bin/sh", "-c", s"cp -R $confSrc/* $confDest")).!

      Seq("tar", "-C", rootDir.getAbsolutePath, "-c", "-z", "-f", s"${targetDir.value}/${name.value}-${version.value}.tar.gz", distDir).!

      pkg
    },
    // when testing, run shellcheck on the scripts in {{{binSrcDir.value}}} directory
    test in Test := {
      Def.taskDyn[Unit] {
        if(enableShellCheck.value) {
          val args = binSrcDir.value
            .listFiles
            .filter(!_.getName.startsWith("."))
            .map(f => "\"\"\"" + f.getAbsolutePath + "\"\"\"")// properly quote the paths
            // Arguments should start with a whitespace in the input task universe
            .mkString(" ", " ", "")

          ShellCheckPlugin.autoImport.shellCheck.toTask(args)
        } else {
          val dummy = Def.taskKey[Unit]("Dummy task to satisfiy sbt")
          dummy
        }
      }.value
      (test in Test).value
    }
  )
}
