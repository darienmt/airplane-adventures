package com.darienmt.airplaneadventures.basestation.collector.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Framing, Source, Tcp}
import akka.util.ByteString
import com.darienmt.airplaneadventures.basestation.collector.parsing.MessageParser
import com.darienmt.airplaneadventures.basestation.data.BaseStation.Message

import scala.collection.immutable.IndexedSeq

object BaseStationSource {

  def apply(address: String, port: Int)(implicit actorSystem: ActorSystem): Source[Message, NotUsed] =
    Source(IndexedSeq(ByteString.empty))
      .via(
        Tcp().outgoingConnection(address, port)
          .via(Framing.delimiter(ByteString("\n"), 256, allowTruncation = true))
          .map(_.utf8String)
      )
      .map(MessageParser(_))

}
