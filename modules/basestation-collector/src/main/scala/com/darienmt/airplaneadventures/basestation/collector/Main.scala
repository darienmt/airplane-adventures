package com.darienmt.airplaneadventures.basestation.collector

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.darienmt.airplaneadventures.basestation.data.BaseStation.Message
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.java8.time.encodeLocalDateDefault
import CirceEncoders._
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}

case class ErrorMessage(message: String)

object Main extends App {
  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val bsAddress = config.getString("station.address")
  val bsPort = config.getInt("station.port")

  val kafkaAddress = config.getString("kafka.address")
  val kafkaPort = config.getString("kafka.port")
  val topic = config.getString("kafka.topic")


  val stringSource = BaseStationSource(bsAddress, bsPort)
    .map {
      case Success(m: Message) => m.asJson
      case Failure(ex) => ErrorMessage(ex.toString()).asJson
    }
    .map(_.noSpaces)
    .map( m => new ProducerRecord[Array[Byte], String](topic, m))


  val producerSettings = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(s"$kafkaAddress:$kafkaPort")


  val end: Future[Done] = stringSource.runWith(Producer.plainSink(producerSettings))
}
