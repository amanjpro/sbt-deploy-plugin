package com.adgear

package object sbt {
  implicit def stringSeqToProcess(seq: Seq[String]) =
    sys.process.stringSeqToProcess(seq)
}

