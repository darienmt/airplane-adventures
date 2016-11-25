package com.darienmt.airplaneadventures.basestation.collector.actors

import akka.Done
import akka.actor.{Actor, ActorLogging, Props}
import com.darienmt.airplaneadventures.basestation.collector.actors.Collector.{StartCollecting, UnknownMessage}
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{SinkConfig, SourceConfig}

import scala.concurrent.Future
import scala.util.Failure
object Collector {

  sealed trait CollectorMessages

  case class StartCollecting(source: SourceConfig, sink: SinkConfig) extends CollectorMessages
  case class UnknownMessage(msg: Any) extends CollectorMessages

  def props(collectorGenerator: (SourceConfig, SinkConfig) => Future[Done]): Props = Props(new Collector(collectorGenerator))

}

class Collector(collectorGenerator: (SourceConfig, SinkConfig) => Future[Done]) extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  override def receive: Receive = readyForCollection orElse unknowMessage

  def readyForCollection: Receive = {
    case StartCollecting(source, sink) => {
      startCollectingMessages(source, sink)
      context.become(waitingForCollectionToFinish(source, sink) orElse unknowMessage, true)
    }
  }

  def unknowMessage: Receive = {
    case msg => sender() ! UnknownMessage(msg)
  }

  def startCollectingMessages(source: SourceConfig, sink: SinkConfig): Unit =
    collectorGenerator(source, sink) pipeTo self

  def waitingForCollectionToFinish(source: SourceConfig, sink: SinkConfig): Receive = {
    case Failure(ex) => {
      log.error(s"Error on stream ${source} => ${sink}", ex)
      startCollectingMessages(source, sink)
    }
    case Done => {
      log.info(s"Stream ${source} => ${sink}, finished. Starting over.")
      startCollectingMessages(source, sink)
    }
  }
}
