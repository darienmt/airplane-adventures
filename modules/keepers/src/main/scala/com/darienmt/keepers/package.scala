package com.darienmt

import akka.Done

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

package object keepers {

  type Generator = () => Future[Done]

  case class RetryConfig(
    maxRetryPeriod: FiniteDuration,
    retryInterval: FiniteDuration,
    retryAutoResetPeriod: FiniteDuration,
    randomIntervalFactor: Double
  )
}
