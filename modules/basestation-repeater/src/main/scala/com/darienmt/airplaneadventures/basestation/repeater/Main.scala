package com.darienmt.airplaneadventures.basestation.repeater

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp
import com.typesafe.config.ConfigFactory

object Main extends App {
  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()

  val bsAddress = config.getString("station.address")
  val bsPort = config.getInt("station.port")

  val upstreamFlow = Tcp().outgoingConnection(bsAddress, bsPort)

  Tcp()
    .bind("0.0.0.0", bsPort)
    .runForeach {
      _
        .flow
        .join(upstreamFlow)
        .run()
    }
}
