package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props, SupervisorStrategy, Terminated}
import com.darienmt.airplaneadventures.basestation.collector.actors.CollectorManager.{StartCollecting, Tick, UnknownMessage}
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{SinkConfig, SourceConfig}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Random
object CollectorManager {

  sealed trait CollectorManagerMessages

  case class StartCollecting(source: SourceConfig, sink: SinkConfig) extends CollectorManagerMessages
  case class UnknownMessage(msg: Any) extends CollectorManagerMessages
  case object Tick extends CollectorManagerMessages

  def props(
             collectorProps: (ActorRef, () => Future[Done]) => Props,
             streamGenerator: (SourceConfig, SinkConfig) => Future[Done],
             maxRetries: Int,
             retryInterval: FiniteDuration
           ): Props = Props(new CollectorManager(collectorProps, streamGenerator, maxRetries, retryInterval))

}

class CollectorManager(
                        collectorProps: (ActorRef, () => Future[Done]) => Props,
                        streamGenerator: (SourceConfig, SinkConfig) => Future[Done],
                        maxRetries: Int,
                        retryInterval: FiniteDuration
                      ) extends Actor with ActorLogging {

  import context.dispatcher

  override val supervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def receive: Receive = readyForCollection orElse unknowMessage

  def readyForCollection: Receive = {
    case StartCollecting(source, sink) => startOver(() => streamGenerator(source, sink))
  }

  def startOver(generator: () => Future[Done], retryCount: Int = 0): Unit = {
    val collector = context.actorOf(collectorProps(self, generator))
    context.watch(collector)
    context.become(waitingForCollectionToFinish(generator, collector, retryCount) orElse unknowMessage, true)
  }

  def unknowMessage: Receive = {
    case msg => sender() ! UnknownMessage(msg)
  }

  def waitingForCollectionToFinish(generator: () => Future[Done], collector: ActorRef, retryCount: Int): Receive = {
    case Collector.StreamFinished => {
      context.unwatch(collector)
      context.stop(collector)
      startOver(generator)
    }
    case Terminated(`collector`) => countTermination(generator, retryCount)
  }

  def countTermination(generator: () => Future[Done], retryCount: Int): Unit =
    if (retryCount == maxRetries) {
      log.error(s"Max retry number reached. System will terminate. Retry count: ${retryCount}")
      context.system.terminate()
    } else {
      val nextRetryCount = retryCount + 1
      log.error(s"Stream finished with error. Retry count: ${retryCount}")
      val nextTry = retryInterval * Math.round(Math.pow(2, Random.nextInt(nextRetryCount)) - 1)
      context.system.scheduler.scheduleOnce(nextTry, self, Tick)
      context.become(waitingForTick(generator, nextRetryCount) orElse unknowMessage, true)
    }

  def waitingForTick(generator: () => Future[Done], retryCount: Int): Receive = {
    case Tick => startOver(generator, retryCount)
  }
}
