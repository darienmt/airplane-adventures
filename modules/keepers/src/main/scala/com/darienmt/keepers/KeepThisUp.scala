package com.darienmt.keepers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.darienmt.keepers.CollectorManager.StartCollecting
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.duration.{ FiniteDuration, MILLISECONDS }

object KeepThisUp {
  def apply(implicit system: ActorSystem, materializer: ActorMaterializer): Generator => Unit =
    apply(ConfigFactory.load())

  def apply(config: Config)(implicit system: ActorSystem, materializer: ActorMaterializer): Generator => Unit = {
    val retryConfig = RetryConfig(
      maxRetryPeriod = FiniteDuration(config.getDuration("retry.maxRetryPeriod").toMillis, MILLISECONDS),
      retryInterval = FiniteDuration(config.getDuration("retry.retryInterval").toMillis, MILLISECONDS),
      retryAutoResetPeriod = FiniteDuration(config.getDuration("retry.retryAutoResetPeriod").toMillis, MILLISECONDS),
      randomIntervalFactor = config.getDouble("retry.randomIntervalFactor")
    )
    apply(retryConfig)(_)
  }

  def apply(retryConfig: RetryConfig)(generator: Generator)(implicit system: ActorSystem, materializer: ActorMaterializer): Unit = {
    val collectorManager = system.actorOf(
      CollectorManager.props(
        Collector.props,
        generator,
        retryConfig
      )
    )
    collectorManager ! StartCollecting
  }
}
