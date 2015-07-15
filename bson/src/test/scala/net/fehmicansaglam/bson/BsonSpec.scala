package net.fehmicansaglam.bson

import java.nio.ByteOrder

import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.element.BsonObjectId
import net.fehmicansaglam.bson.reader.BsonDocumentReader
import org.joda.time.DateTime
import org.scalatest.OptionValues._
import org.scalatest.{Matchers, WordSpec}

class BsonSpec extends WordSpec with Matchers {

  "Bson" must {

    val document = $document(
      "_id" := BsonObjectId.generate,
      "name" := "jack",
      "age" := 18,
      "months" := $array(1, 2, 3),
      "details" := $document(
        "salary" := 455.5,
        "inventory" := $array("a", 3.5, 1L, true),
        "birthday" := new DateTime(1987, 3, 5, 0, 0),
        "personal" := $document(
          "foo" := "bar"
        )
      )
    )

    "encode and decode BsonDocument" in {
      val encoded = document.encode
      val buffer = encoded.asByteBuffer
      buffer.order(ByteOrder.LITTLE_ENDIAN)
      val actual = BsonDocumentReader.read(buffer)
      actual.value shouldBe document
    }

    "get nested values" in {
      document.getAs[Int]("age").value shouldBe 18
      document.getAs[Double]("details.salary").value shouldBe 455.5
      document.getAs[String]("details.personal.foo").value shouldBe "bar"
      document.getAsList[Any]("details.inventory").value shouldBe List("a", 3.5, 1L, true)

      val details = document.getAs[BsonDocument]("details").get
      details.getAs[Double]("salary").value shouldBe 455.5

      val personal = document.getAs[BsonDocument]("details.personal").get
      personal.getAs[String]("foo").value shouldBe "bar"
    }

    "handle (deeply) nested collections" in {
      val map = Map(
        "level0" -> 1,
        "level1" -> Map(
          "level2" -> "2"
        )
      )
      val doc = BsonDocument.from(map.toIterable)
      doc.toJson() shouldBe """{ "level0": 1, "level1": { "level2": "2" } }"""

      val map2 = Map(
        "level1" -> List(Map(
          "level2" -> "2"
        )),
        "listoflists" -> List(List(1, 2), List(3, 4))
      )
      val doc2 = BsonDocument.from(map2.toIterable)
      doc2.toJson() shouldBe """{ "level1": [ { "level2": "2" } ], "listoflists": [ [ 1, 2 ], [ 3, 4 ] ] }"""
    }
  }
}
