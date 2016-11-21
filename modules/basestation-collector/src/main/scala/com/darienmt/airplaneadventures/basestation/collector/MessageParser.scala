package com.darienmt.airplaneadventures.basestation.collector

import com.darienmt.airplaneadventures.basestation.collector.FromCSVtoCaseClass._
import com.darienmt.airplaneadventures.basestation.data.BaseStation._

import scala.util.{Failure, Try}

object MessageParser {
  def apply(l: String): Try[Message] = parse(l.split(",").toList)

  protected val parse: List[String] => Try[Message] = {
    case "SEL"   :: ""  :: rest => rowParserFor[SelectionChangeMessage](masked(rest,  List(3,4,5,6,7,8,9,10,11)))
    case "ID"    :: ""  :: rest => rowParserFor[IdMessage](masked(rest,  List(3,4,5,6,7,8,9,10,11)))
    case "AIR"   :: ""  :: rest => rowParserFor[NewAircraftMessage](masked(rest,  List(3,4,5,6,7,8,9,10)))
    case "STA"   :: ""  :: rest => rowParserFor[StatusChangeMessage](masked(rest,  List(3,4,5,6,7,8,9,10,11)))
    case "CLK"   :: ""  :: rest => rowParserFor[ClickMessage](masked(rest,  List(3,7,8,9,10)))
    case "MSG"   :: "1" :: rest => rowParserFor[ESIdentificationAncCategoryMessage](masked(rest, List(3,4,5,6,7,8,9,10,11)))
    case "MSG"   :: "2" :: rest => rowParserFor[ESSurfacePositionMessage](masked(rest, List(3,4,5,6,7,8,9,10,12,13,14,15,16,22)))
    case "MSG"   :: "3" :: rest => rowParserFor[ESAirbornePositionMessage](masked(rest, List(3,4,5,6,7,8,9,10,12,15,16,19,20,21,22)))
    case "MSG"   :: "4" :: rest => rowParserFor[ESAirborneVelocityMessage](masked(rest, List(3,4,5,6,7,8,9,10,13,14,17)))
    case "MSG"   :: "5" :: rest => rowParserFor[SurveillanceAltMessage](masked(rest, List(3,4,5,6,7,8,9,10,12,19,21,22)))
    case "MSG"   :: "6" :: rest => rowParserFor[SurveillanceIdMessage](masked(rest, List(3,4,5,6,7,8,9,10,12,18,19,20,21,22)))
    case "MSG"   :: "7" :: rest => rowParserFor[AirToAirMessage](masked(rest, List(3,4,5,6,7,8,9,10,12,22)))
    case "MSG"   :: "8" :: rest => rowParserFor[AllCallReplyMessage](masked(rest, List(3,4,5,6,7,8,9,10,22)))
    case x => Failure(new Exception(s"Unknown message => $x"))
  }

  protected def masked(l: List[String], mask: List[Int]) = mask.map( i => l(i -3))
}
