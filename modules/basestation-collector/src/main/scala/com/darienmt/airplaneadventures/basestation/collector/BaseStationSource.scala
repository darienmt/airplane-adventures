package com.darienmt.airplaneadventures.basestation.collector

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.scaladsl.{ Flow, Framing, Source, Tcp }
import akka.util.ByteString
import com.darienmt.airplaneadventures.basestation.data.BaseStation.Message

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future
import scala.util.Try

object BaseStationSource {

  def apply(address: String, port: Int)(implicit actorSystem: ActorSystem): Source[Try[Message], NotUsed] =
    Source(IndexedSeq(ByteString.empty))
      .via(
        Tcp().outgoingConnection(address, port)
          .via(Framing.delimiter(ByteString("\n"), 256))
          .map(_.utf8String)
      )
      .map(MessageParser(_))

}
