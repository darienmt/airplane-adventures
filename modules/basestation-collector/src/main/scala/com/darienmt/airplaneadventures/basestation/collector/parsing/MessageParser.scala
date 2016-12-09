package com.darienmt.airplaneadventures.basestation.collector.parsing

import FromCSVtoCaseClass.rowParserFor
import com.darienmt.airplaneadventures.basestation.data.BaseStation._

import scala.util.{ Failure, Success, Try }

object MessageParser {
  def apply(l: String): Message = parse(l.split(",").toList) match {
    case Success(m) => m
    case Failure(ex) => ErrorMessage(l, ex.toString)
  }

  protected val parse: List[String] => Try[Message] = {
    case "SEL" :: "" :: rest => rowParserFor[SelectionChangeMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 11))
    case "ID" :: "" :: rest => rowParserFor[IdMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 11))
    case "AIR" :: "" :: rest => rowParserFor[NewAircraftMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10))
    case "STA" :: "" :: rest => rowParserFor[StatusChangeMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 11))
    case "CLK" :: "" :: rest => rowParserFor[ClickMessage](rest, List(3, 7, 8, 9, 10))
    case "MSG" :: "1" :: rest => rowParserFor[ESIdentificationAncCategoryMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 11))
    case "MSG" :: "2" :: rest => rowParserFor[ESSurfacePositionMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 22))
    case "MSG" :: "3" :: rest => rowParserFor[ESAirbornePositionMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 12, 15, 16, 19, 20, 21, 22))
    case "MSG" :: "4" :: rest => rowParserFor[ESAirborneVelocityMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 13, 14, 17))
    case "MSG" :: "5" :: rest => rowParserFor[SurveillanceAltMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 12, 19, 21, 22))
    case "MSG" :: "6" :: rest => rowParserFor[SurveillanceIdMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 12, 18, 19, 20, 21, 22))
    case "MSG" :: "7" :: rest => rowParserFor[AirToAirMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 12, 22))
    case "MSG" :: "8" :: rest => rowParserFor[AllCallReplyMessage](rest, List(3, 4, 5, 6, 7, 8, 9, 10, 22))
    case x => Failure(new Exception(s"Unknown message => $x"))
  }
}
