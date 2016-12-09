package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorLogging, ActorRef, Cancellable, Props }
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector.{ Generator, StreamFinished, UnknownMessage }

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Collector {
  type Generator = () => Future[Done]

  sealed trait CollectorMessage
  case object StreamFinished extends CollectorMessage
  case class UnknownMessage(msg: Any) extends CollectorMessage

  def props(
    manager: ActorRef,
    collectorGenerator: Generator
  ): Props = Props(new Collector(manager, collectorGenerator))
}

class Collector(
    manager: ActorRef,
    collectorGenerator: Generator
) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case Failure(ex) => {
      log.error(ex, "Stream stopped")
      throw ex
    }
    case Done => {
      manager ! StreamFinished
    }
    case msg => manager ! UnknownMessage(msg)
  }

  override def preStart(): Unit = collectorGenerator() pipeTo self

  override def postRestart(reason: Throwable): Unit = {}

}
