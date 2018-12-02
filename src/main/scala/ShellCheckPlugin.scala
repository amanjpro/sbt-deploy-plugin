package me.amanj.sbt

import sbt._
import sbt.complete.DefaultParsers

object ShellCheckPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = noTrigger

  object autoImport {
    lazy val shellCheck = inputKey[Unit]("Execute shellcheck")
    lazy val shellCheckArgs = settingKey[Seq[String]]("Extra arguments to shellcheck")
  }

  import autoImport._

  override lazy val buildSettings = Seq(
    shellCheckArgs := Seq.empty,
    shellCheck := {
      val extraArgs = shellCheckArgs.value.toList
      val args = DefaultParsers.spaceDelimited("<arg>").parsed.toList
      if(!args.isEmpty && ("shellcheck" :: (extraArgs ++ args)).! != 0)
        throw new IllegalStateException("shellcheck failed")
    }
  )
}
