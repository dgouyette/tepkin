package net.fehmicansaglam.tepkin

object TepkinMessages {

  sealed trait TepkinMessage

  case object InitPool extends TepkinMessage

  case object Idle extends TepkinMessage

  case object ShutDown extends TepkinMessage

  case object ConnectFailed extends TepkinMessage

  case object WriteFailed extends TepkinMessage

  case object ConnectionClosed extends TepkinMessage

}