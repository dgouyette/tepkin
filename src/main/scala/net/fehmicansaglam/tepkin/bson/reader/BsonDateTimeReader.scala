package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonDateTime
import org.joda.time.DateTime

case class BsonDateTimeReader(buffer: ByteBuffer) extends Reader[BsonDateTime] {
  override def read: Option[BsonDateTime] = {
    val name = readCString()
    val value = buffer.getLong()
    Some(BsonDateTime(name, new DateTime(value)))
  }
}
