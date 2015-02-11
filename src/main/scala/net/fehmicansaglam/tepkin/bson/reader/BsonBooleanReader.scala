package net.fehmicansaglam.tepkin.bson.reader

import java.nio.ByteBuffer

import net.fehmicansaglam.tepkin.bson.element.BsonBoolean

case class BsonBooleanReader(buffer: ByteBuffer) extends Reader[BsonBoolean] {

  def read: Option[BsonBoolean] = {
    val name = readCString()
    Some {
      buffer.get() match {
        case 0x00 => BsonBoolean(name, false)
        case 0x01 => BsonBoolean(name, true)
      }
    }
  }
}
