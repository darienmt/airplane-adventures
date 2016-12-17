package com.darienmt.airplaneadventures.basestation.repeater

import akka.stream.scaladsl.Tcp
import com.darienmt.keepers.MainCommons

object Main extends App with MainCommons {

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
