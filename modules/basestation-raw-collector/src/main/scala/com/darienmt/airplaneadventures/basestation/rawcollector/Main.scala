package com.darienmt.airplaneadventures.basestation.rawcollector

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{ Framing, Source, Tcp }
import akka.util.ByteString
import com.darienmt.keepers.{ Generator, KeepThisUp, MainCommons }
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

import scala.collection.immutable.IndexedSeq

object Main extends App with MainCommons {

  val bsAddress = config.getString("station.address")
  val bsPort = config.getInt("station.port")

  val kafkaAddress = config.getString("kafka.address")
  val kafkaPort = config.getInt("kafka.port")
  val kafkaTopic = config.getString("kafka.topic")

  val generator: Generator = () => Source(IndexedSeq(ByteString.empty))
    .via(
      Tcp().outgoingConnection(bsAddress, bsPort)
        .via(Framing.delimiter(ByteString("\n"), 256, allowTruncation = true))
        .map(_.utf8String)
    )
    .map(m => new ProducerRecord[Array[Byte], String](kafkaTopic, m))
    .runWith(
      Producer.plainSink(
        ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
          .withBootstrapServers(s"${kafkaAddress}:${kafkaPort}")
      )
    )

  //noinspection ScalaStyle
  val justGettingData: Generator = () => Source(IndexedSeq(ByteString.empty))
    .via(
      Tcp().outgoingConnection(bsAddress, bsPort)
        .via(Framing.delimiter(ByteString("\n"), 256, allowTruncation = true))
        .map(_.utf8String)
    )
    .runForeach(println)

  val keeper = KeepThisUp(config)
  keeper(generator)
}
