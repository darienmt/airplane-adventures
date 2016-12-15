package com.darienmt.airplaneadventures.basestation.collector.streams

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.ActorMaterializer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ ByteArraySerializer, StringSerializer }

import scala.concurrent.Future
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.java8.time.{ encodeLocalDateDefault, encodeZonedDateTimeDefault }
import com.darienmt.airplaneadventures.basestation.collector.parsing.CirceEncoders._

object BaseStation2Kafka {

  case class SourceConfig(address: String, port: Int)
  case class SinkConfig(address: String, port: Int, topic: String)

  def apply(sourceConfig: SourceConfig, sinkConfig: SinkConfig)(implicit system: ActorSystem, materializer: ActorMaterializer): Future[Done] =
    BaseStationSource(sourceConfig.address, sourceConfig.port)
      .map(_.asJson.noSpaces)
      .map(m => new ProducerRecord[Array[Byte], String](sinkConfig.topic, m))
      .runWith(
        Producer.plainSink(
          ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
            .withBootstrapServers(s"${sinkConfig.address}:${sinkConfig.port}")
        )
      )
}
