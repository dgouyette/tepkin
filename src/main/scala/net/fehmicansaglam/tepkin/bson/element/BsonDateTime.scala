package net.fehmicansaglam.tepkin.bson.element

import net.fehmicansaglam.tepkin.bson.Implicits.BsonValueDateTime

case class BsonDateTime(name: String, value: BsonValueDateTime) extends BsonElement {
  val code: Byte = 0x09
}
