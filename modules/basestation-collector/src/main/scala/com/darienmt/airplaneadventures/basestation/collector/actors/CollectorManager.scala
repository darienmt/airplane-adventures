package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.{ Actor, ActorContext, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated }
import akka.pattern.{ Backoff, BackoffSupervisor }
import com.darienmt.airplaneadventures.basestation.collector.actors.CollectorManager._
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{ SinkConfig, SourceConfig }

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
object CollectorManager {

  type CollectorProps = (ActorRef, Collector.Generator) => Props
  type StreamGenerator = (SourceConfig, SinkConfig) => Future[Done]

  sealed trait CollectorManagerMessages

  case class RetryConfig(
    maxRetryPeriod: FiniteDuration,
    retryInterval: FiniteDuration,
    retryAutoResetPeriod: FiniteDuration,
    randomIntervalFactor: Double
  )
  case class StartCollecting(source: SourceConfig, sink: SinkConfig) extends CollectorManagerMessages
  case class UnknownMessage(msg: Any) extends CollectorManagerMessages

  def props(
    collectorProps: CollectorProps,
    streamGenerator: StreamGenerator,
    retryConfig: RetryConfig
  ): Props = Props(new CollectorManager(collectorProps, streamGenerator, retryConfig))

}

class CollectorManager(
    collectorProps: CollectorProps,
    streamGenerator: StreamGenerator,
    retryConfig: RetryConfig
) extends Actor with ActorLogging {

  override def receive: Receive = readyForCollection orElse unknowMessage

  def readyForCollection: Receive = {
    case StartCollecting(source, sink) => startOver(() => streamGenerator(source, sink))
  }

  def startOver(generator: () => Future[Done]): Unit = {
    val supervisorProps = BackoffSupervisor.props(
      Backoff.onFailure(
        collectorProps(self, generator),
        childName = "collector",
        minBackoff = retryConfig.retryInterval,
        maxBackoff = retryConfig.maxRetryPeriod,
        randomFactor = retryConfig.randomIntervalFactor
      )
        .withAutoReset(retryConfig.retryAutoResetPeriod)
    )
    val supervisor = context.actorOf(supervisorProps)
    context.become(waitingForCollectionToFinish(generator, supervisor) orElse unknowMessage, true)
  }

  def unknowMessage: Receive = {
    case msg => sender() ! UnknownMessage(msg)
  }

  def waitingForCollectionToFinish(generator: () => Future[Done], supervisor: ActorRef): Receive = {
    case Collector.StreamFinished => {
      context.unwatch(supervisor)
      context.stop(supervisor)
      startOver(generator)
    }
    case Collector.UnknownMessage(msg) => log.error("Unknown message received by collector => " + msg.toString)
  }

}
