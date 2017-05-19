package com.adgear.sbt

import sbt._
import sbt.complete.DefaultParsers

object AdGearShellCheckPlugin extends AutoPlugin {
  override def requires = plugins.JvmPlugin
  override def trigger  = noTrigger

  object autoImport {
    lazy val shellCheck = inputKey[Unit]("Execute shellcheck")
  }

  import autoImport._

  override lazy val buildSettings = Seq(
    shellCheck := {
      val args = DefaultParsers.spaceDelimited("<arg>").parsed.toList
      if(("shellcheck" :: args).! != 0)
        throw new IllegalStateException("shellcheck failed")
    }
  )
}
