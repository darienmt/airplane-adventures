package com.darienmt.keepers

import akka.Done
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.{ Backoff, BackoffSupervisor }
import com.darienmt.keepers.CollectorManager.{ CollectorProps, StartCollecting, UnknownMessage }

import scala.concurrent.Future
object CollectorManager {

  type CollectorProps = (ActorRef, Generator) => Props

  sealed trait CollectorManagerMessages
  case object StartCollecting extends CollectorManagerMessages
  case class UnknownMessage(msg: Any) extends CollectorManagerMessages

  def props(
    collectorProps: CollectorProps,
    generator: Generator,
    retryConfig: RetryConfig
  ): Props = Props(new CollectorManager(collectorProps, generator, retryConfig))

}

class CollectorManager(
    collectorProps: CollectorProps,
    generator: Generator,
    retryConfig: RetryConfig
) extends Actor with ActorLogging {

  override def receive: Receive = readyForCollection orElse unknowMessage

  def readyForCollection: Receive = {
    case StartCollecting => startOver(generator)
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
  }

}
