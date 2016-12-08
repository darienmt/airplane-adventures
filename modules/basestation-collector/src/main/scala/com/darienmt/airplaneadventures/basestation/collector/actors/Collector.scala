package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector.{Generator, StreamFinished, Tick, UnknownMessage}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Collector {
  type Generator = () => Future[Done]

  sealed trait CollectorMessage
  case object StreamFinished extends CollectorMessage
  case class UnknownMessage(msg: Any) extends CollectorMessage
  case object Tick extends CollectorMessage

  def props(
             manager: ActorRef,
             collectorGenerator: Generator,
             heartbeatInterval: FiniteDuration
           ): Props = Props(new Collector(manager, collectorGenerator, heartbeatInterval))
}

class Collector(
                 manager: ActorRef,
                 collectorGenerator: Generator,
                 heartbeatInterval: FiniteDuration
               ) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  var schedulerHandler: Option[Cancellable] = None

  override def receive: Receive = {
    case Failure(ex) => {
      log.error(ex, "Stream stopped")
      throw ex
    }
    case Done => {
      manager ! StreamFinished
    }
    case Tick => {
      scheduleTick()
      manager ! Tick
    }
    case msg => UnknownMessage(msg)
  }

  def startCollectingMessages(): Unit =
    collectorGenerator() pipeTo self

  override def preStart(): Unit = {
    scheduleTick()
    startCollectingMessages()
  }

  protected def scheduleTick(): Unit =
    schedulerHandler = Some(context.system.scheduler.scheduleOnce(heartbeatInterval, self, Tick))

  override def postRestart(reason: Throwable): Unit = {}

  override def postStop(): Unit = {
    schedulerHandler.foreach(_.cancel())
    super.postStop()
  }

}
