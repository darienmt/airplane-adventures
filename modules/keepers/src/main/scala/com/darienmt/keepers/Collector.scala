package com.darienmt.keepers

import akka.Done
import akka.actor.Status.Failure
import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.darienmt.keepers.Collector.{ StreamFinished }

object Collector {
  sealed trait CollectorMessage
  case object StreamFinished extends CollectorMessage

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
    case msg => throw new Exception("Unknown message: " + msg.toString)
  }

  override def preStart(): Unit = collectorGenerator() pipeTo self

  override def postRestart(reason: Throwable): Unit = {}

}
