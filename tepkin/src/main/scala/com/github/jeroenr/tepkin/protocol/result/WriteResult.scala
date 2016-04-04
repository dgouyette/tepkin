package com.github.jeroenr.tepkin.protocol.result

import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.tepkin.protocol.exception.{WriteConcernException, WriteException}

trait CodeAndErrorMsg {
  val code: Int
  val errmsg: String
}

case class Error(code: Int, errmsg: String) extends CodeAndErrorMsg

object Error {
  def apply(document: BsonDocument): Option[Error] = {
    for {
      code <- document.getAs[Int]("code")
      errMsg <- document.getAs[String]("errmsg")
    } yield Error(code, errMsg)
  }
}

case class WriteError(code: Int, errmsg: String) extends CodeAndErrorMsg

object WriteError {
  def apply(document: BsonDocument): WriteError = {
    WriteError(
      document.getAs[Int]("code").get,
      document.getAs[String]("errmsg").get)
  }
}

case class WriteConcernError(code: Int, errInfo: BsonDocument, errmsg: String) extends CodeAndErrorMsg

object WriteConcernError {
  def apply(document: BsonDocument): WriteConcernError = {
    WriteConcernError(
      document.getAs[Int]("code").get,
      document.getAs[BsonDocument]("errInfo").get,
      document.getAs[String]("errmsg").get)
  }
}

trait WriteResult extends Result {
  def ok: Boolean

  def n: Int

  def writeErrors: Option[List[WriteError]]

  def writeConcernError: Option[WriteConcernError]

  def hasWriteError: Boolean = writeErrors.exists(_.nonEmpty)

  def hasWriteConcernError: Boolean = writeConcernError.isDefined

  /**
   * Used to explicitly throw an exception when the result is not OK.
   */
  def convertErrorToException(): this.type = {
    if (!ok && !hasWriteError && !hasWriteConcernError) {
      throw new IllegalStateException("Result is not OK but there are no errors.")
    }

    if (hasWriteError) throw WriteException(writeErrors.get)
    if (hasWriteConcernError) throw WriteConcernException(writeConcernError.get)

    this
  }

}
