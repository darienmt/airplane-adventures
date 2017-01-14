package com.darienmt.airplaneadventures.basestation.rawcollector

import java.net.InetSocketAddress

import akka.Done
import akka.io.Inet.SocketOption
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.{ Framing, Source, Tcp }
import akka.util.ByteString
import com.darienmt.keepers.{ Generator, KeepThisUp, MainCommons }
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

import scala.collection.immutable
import scala.collection.immutable.IndexedSeq
import scala.util.Success
import scala.concurrent.duration._

object Main extends App with MainCommons {

  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)

  val bsAddress = config.getString("station.address")
  val bsPort = config.getInt("station.port")
  val connectTimeout: Duration = config.getDuration("station.connectTimeout")
  val idleTimeout: Duration = config.getDuration("station.idleTimeout")

  val kafkaAddress = config.getString("kafka.address")
  val kafkaPort = config.getInt("kafka.port")
  val kafkaTopic = config.getString("kafka.topic")


  val generator: Generator = () => Source(IndexedSeq(ByteString.empty))
    .via(
      Tcp().outgoingConnection(
        remoteAddress = InetSocketAddress.createUnresolved(bsAddress, bsPort),
        connectTimeout = connectTimeout,
        idleTimeout = idleTimeout
      )
        .via(Framing.delimiter(ByteString("\n"), 256))
        .map(_.utf8String)
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
