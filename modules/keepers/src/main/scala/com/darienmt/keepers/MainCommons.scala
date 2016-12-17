package com.darienmt.keepers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import kamon.Kamon

trait MainCommons {
  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  Kamon.start()
  system.whenTerminated.andThen {
    case _ => Kamon.shutdown()
  }

  val config = ConfigFactory.load()
}
