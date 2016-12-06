package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.Status.Failure
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector.{StreamFinished, UnknownMessage}

import scala.concurrent.Future

object Collector {

  sealed trait CollectorMessage
  case object StreamFinished extends CollectorMessage
  case class UnknownMessage(msg: Any) extends CollectorMessage

  def props(manager: ActorRef, collectorGenerator: () => Future[Done]): Props = Props(new Collector(manager, collectorGenerator))
}

class Collector(manager: ActorRef, collectorGenerator: () => Future[Done]) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = {
    case Failure(ex) => throw ex
    case Done => {
      manager ! StreamFinished
    }
    case msg => UnknownMessage(msg)
  }

  def startCollectingMessages(): Unit =
    collectorGenerator() pipeTo self

  override def preStart(): Unit = startCollectingMessages()

}
