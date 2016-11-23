package com.darienmt.airplaneadventures.basestation.collector

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.darienmt.airplaneadventures.basestation.data.BaseStation.Message
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.java8.time.{encodeLocalDateDefault}
import CirceEncoders._

object Main extends App {
  implicit val system = ActorSystem("collector")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val config = ConfigFactory.load()
  val address = config.getString("station.address")
  val port = config.getInt("station.port")


  val stringSource = BaseStationSource(address, port)
    .map {
      case Success(m: Message) => m.asJson.spaces2
      case Failure(ex) => ex.toString()
    }

  val realSink: Sink[Try[Message], Future[Done]] = Sink.foreach( {
    case Success(m) => println(m.getClass.getSimpleName + " => " + m.toString)
    case Failure(ex) => println("Error!!!" )
  } )

  val end: Future[Done] = stringSource.runWith(Sink.foreach(println))
}
