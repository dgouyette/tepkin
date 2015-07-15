package net.fehmicansaglam.tepkin

import java.io.{File, FileInputStream}
import java.security.MessageDigest

import akka.stream.ActorMaterializer
import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.util.Converters
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.duration._

class GridFsSpec
  extends FlatSpec
  with Matchers
  with ScalaFutures
  with OptionValues
  with BeforeAndAfter
  with BeforeAndAfterAll {

  override implicit val patienceConfig = PatienceConfig(timeout = 30.seconds, interval = 1.seconds)

  val client = MongoClient("mongodb://localhost")
  val db = client("tepkin")
  val fs = db.gridFs()

  import client.{context, ec}

  implicit val timeout: Timeout = 30.seconds

  before {
    Await.ready(db.collection("fs.files").drop(), 5.seconds)
    Await.ready(db.collection("fs.chunks").drop(), 5.seconds)
    Thread.sleep(2000)
  }

  after {
    Await.ready(db.collection("fs.files").drop(), 5.seconds)
    Await.ready(db.collection("fs.chunks").drop(), 5.seconds)
  }

  override protected def afterAll() = client.shutdown()

  "A GridFs" should "put and find and delete File" in {
    implicit val mat = ActorMaterializer()

    val result = for {
      put <- fs.put(new File(getClass.getResource("/sample.pdf").getPath))
      file <- fs.findOne("filename" := "sample.pdf")
      delete <- fs.deleteOne("filename" := "sample.pdf")
    } yield (put, file, delete)

    whenReady(result) { case (put, file, delete) =>
      file shouldBe 'defined
      file.get.get[BsonValueObjectId]("_id") shouldBe put.get[BsonValueObjectId]("_id")
      delete.n shouldBe 1
    }
  }

  it should "put and get and delete File" in {
    implicit val mat = ActorMaterializer()

    val result = for {
      put <- fs.put(new File(getClass.getResource("/sample.pdf").getPath))
      source <- fs.getOne("filename" := "sample.pdf")
      md = MessageDigest.getInstance("MD5")
      unit <- source.get.runForeach { chunk =>
        md.update(chunk.data.identifier.toArray)
        ()
      }
      md5 = md.digest()
      delete <- fs.deleteOne("filename" := "sample.pdf")
    } yield (put, md5, delete)

    whenReady(result) { case (put, md5, delete) =>
      Converters.hex2Str(md5) shouldBe put.getAs[String]("md5").get
      delete.n shouldBe 1
    }
  }

  it should "put and get and delete FileInputStream" in {
    implicit val mat = ActorMaterializer()

    val result = for {
      put <- fs.put("sample.pdf", new FileInputStream(getClass.getResource("/sample.pdf").getPath))
      source <- fs.getOne("filename" := "sample.pdf")
      md = MessageDigest.getInstance("MD5")
      unit <- source.get.runForeach { chunk =>
        md.update(chunk.data.identifier.toArray)
        ()
      }
      md5 = md.digest()
      delete <- fs.deleteOne("filename" := "sample.pdf")
    } yield (put, md5, delete)

    whenReady(result) { case (put, md5, delete) =>
      Converters.hex2Str(md5) shouldBe put.getAs[String]("md5").get
      delete.n shouldBe 1
    }
  }

}
