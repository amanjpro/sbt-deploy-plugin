package me.amanj

package object sbt {
  implicit def stringSeqToProcess(seq: Seq[String]) =
    sys.process.stringSeqToProcess(seq)
}

