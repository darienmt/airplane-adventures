package com.darienmt.airplaneadventures.basestation.collector

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.darienmt.airplaneadventures.basestation.collector.actors.{ Collector, CollectorManager }
import com.darienmt.airplaneadventures.basestation.collector.actors.CollectorManager.{ RetryConfig, StartCollecting }
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{ SinkConfig, SourceConfig }
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()

  val sourceConfig = SourceConfig(
    config.getString("station.address"),
    config.getInt("station.port")
  )

  val sinkConfig = SinkConfig(
    config.getString("kafka.address"),
    config.getInt("kafka.port"),
    config.getString("kafka.topic")
  )

  val retryConfig = RetryConfig(
    maxRetryPeriod = FiniteDuration(config.getDuration("retry.maxRetryPeriod").toMillis, MILLISECONDS),
    retryInterval = FiniteDuration(config.getDuration("retry.retryInterval").toMillis, MILLISECONDS),
    retryAutoResetPeriod = FiniteDuration(config.getDuration("retry.retryAutoResetPeriod").toMillis, MILLISECONDS),
    randomIntervalFactor = config.getDouble("retry.randomIntervalFactor")
  )

  val collectorManager = system.actorOf(
    CollectorManager.props(
      Collector.props,
      BaseStation2Kafka.apply,
      retryConfig
    )
  )
  collectorManager ! StartCollecting(sourceConfig, sinkConfig)
}
