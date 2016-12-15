package com.darienmt.airplaneadventures.basestation.rawcollector

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.{ ActorAttributes, ActorMaterializer, Supervision }
import akka.stream.scaladsl.{ Framing, Source, Tcp }
import akka.util.ByteString
import com.darienmt.keepers.{ Generator, KeepThisUp }
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

import scala.collection.immutable.IndexedSeq
import scala.util.{ Failure, Success }

object Main extends App {

  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()

  val bsAddress = config.getString("station.address")
  val bsPort = config.getInt("station.port")

  val kafkaAddress = config.getString("kafka.address")
  val kafkaPort = config.getInt("kafka.port")
  val kafkaTopic = config.getString("kafka.topic")

  //noinspection ScalaStyle
  val generator: Generator = () => Source(IndexedSeq(ByteString.empty))
    .via(
      Tcp().outgoingConnection(bsAddress, bsPort)
        .via(Framing.delimiter(ByteString("\n"), 256, allowTruncation = true))
        .map(_.utf8String)
        .withAttributes(ActorAttributes.supervisionStrategy {
          case ex: Throwable =>
            println("Error ocurred: " + ex)
            Supervision.Resume
        })
    )
    .map(m => new ProducerRecord[Array[Byte], String](kafkaTopic, m))
    .runWith(
      Producer.plainSink(
        ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
          .withBootstrapServers(s"${kafkaAddress}:${kafkaPort}")
      )
    )

  val keeper = KeepThisUp(config)
  keeper(generator)
}
