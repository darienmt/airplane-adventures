package com.darienmt.airplaneadventures.basestation.data

import java.time.{LocalDate, LocalTime}

/**
  *  More information regarding BaseStation format could be found at:
  *  http://woodair.net/sbs/Article/Barebones42_Socket_Data.htm
***/
object BaseStation {

  sealed trait Message

  /**
    * SELECTION CHANGE MESSAGE :  Generated when the user changes the selected aircraft in BaseStation. (SEL)
    */
  case class SelectionChangeMessage(
                                     sessionId: String,
                                     aircraftId: String,
                                     hexIndent: String,
                                     flightId: String,
                                     dateMessageGenerated: LocalDate,
                                     timeMessageGenerated: LocalTime,
                                     dateMessageLogged: LocalDate,
                                     timeMessageLogged: LocalTime,
                                     callSign: String
                                   ) extends Message


  /**
    *  NEW ID MESSAGE : Generated when an aircraft being tracked sets or changes its callsign. (ID)
    */
  final case class IdMessage(
                              sessionId: String,
                              aircraftId: String,
                              hexIndent: String,
                              flightId: String,
                              dateMessageGenerated: LocalDate,
                              timeMessageGenerated: LocalTime,
                              dateMessageLogged: LocalDate,
                              timeMessageLogged: LocalTime,
                              callSign: String
                            ) extends Message


  /**
    *  NEW AIRCRAFT MESSAGE : Generated when the SBS picks up a signal
    *                         for an aircraft that it isn't currently tracking. (AIR)
    */
  final case class NewAircraftMessage(
                                       sessionId: String,
                                       aircraftId: String,
                                       hexIndent: String,
                                       flightId: String,
                                       dateMessageGenerated: LocalDate,
                                       timeMessageGenerated: LocalTime,
                                       dateMessageLogged: LocalDate,
                                       timeMessageLogged: LocalTime
                                     ) extends Message

  /**
    *   STATUS CHANGE MESSAGE : Generated when an aircraft's status changes according to the time-out
    *                           values in the Data Settings menu. (STA)
    *
    *   recordStatusFlag => Values are
    *   - PL (Position Lost)
    *   - SL (Signal Lost)
    *   - RM (Remove)
    *   - AD (Delete)
    *   - OK (used to reset time-outs if aircraft returns into cover).
    */
  final case class StatusChangeMessage(
                                        sessionId: String,
                                        aircraftId: String,
                                        hexIndent: String,
                                        flightId: String,
                                        dateMessageGenerated: LocalDate,
                                        timeMessageGenerated: LocalTime,
                                        dateMessageLogged: LocalDate,
                                        timeMessageLogged: LocalTime,
                                        recordStatusFlag: String
                                      ) extends Message

  /**
    *  CLICK MESSAGE : Generated when the user double-clicks (or presses return) on an aircraft
    *                  (i.e. to bring up the aircraft details window). (CLK)
    */
  final case class ClickMessage(
                                 sessionId: String,
                                 dateMessageGenerated: LocalDate,
                                 timeMessageGenerated: LocalTime,
                                 dateMessageLogged: LocalDate,
                                 timeMessageLogged: LocalTime
                               ) extends Message


  sealed trait TransmissionMessage extends Message

  type isOnGroundType = Boolean

  /**
    *  Transmission Message - ES Identification and Category (MSG1)
    */
  final case class ESIdentificationAncCategoryMessage(
                                                       sessionId: String,
                                                       aircraftId: String,
                                                       hexIndent: String,
                                                       flightId: String,
                                                       dateMessageGenerated: LocalDate,
                                                       timeMessageGenerated: LocalTime,
                                                       dateMessageLogged: LocalDate,
                                                       timeMessageLogged: LocalTime,
                                                       callSign: String
                                                     ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - ES Surface Position Message (MSG2)
    */
  final case class ESSurfacePositionMessage(
                                             sessionId: String,
                                             aircraftId: String,
                                             hexIndent: String,
                                             flightId: String,
                                             dateMessageGenerated: LocalDate,
                                             timeMessageGenerated: LocalTime,
                                             dateMessageLogged: LocalDate,
                                             timeMessageLogged: LocalTime,
                                             altitude: Int,
                                             groundSpeed: String,
                                             track: String,
                                             latitude: String,
                                             longitude: String,
                                             isOnGround: isOnGroundType
                                           ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - ES Airborne Position Message (MSG3)
    */
  final case class ESAirbornePositionMessage(
                                              sessionId: String,
                                              aircraftId: String,
                                              hexIndent: String,
                                              flightId: String,
                                              dateMessageGenerated: LocalDate,
                                              timeMessageGenerated: LocalTime,
                                              dateMessageLogged: LocalDate,
                                              timeMessageLogged: LocalTime,
                                              altitude: Int,
                                              latitude: String,
                                              longitude: String,
                                              squawkChanged: String,
                                              emergency: String,
                                              transponderIdent: String,
                                              isOnGround: isOnGroundType
                                            ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - ES Airborne Velocity Message (MSG4)
    */
  final case class ESAirborneVelocityMessage(
                                              sessionId: String,
                                              aircraftId: String,
                                              hexIndent: String,
                                              flightId: String,
                                              dateMessageGenerated: LocalDate,
                                              timeMessageGenerated: LocalTime,
                                              dateMessageLogged: LocalDate,
                                              timeMessageLogged: LocalTime,
                                              groundSpeed: String,
                                              track: String,
                                              verticalRate: String
                                            ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - Surveillance Alt Message (MSG5)
    */
  final case class SurveillanceAltMessage(
                                           sessionId: String,
                                           aircraftId: String,
                                           hexIndent: String,
                                           flightId: String,
                                           dateMessageGenerated: LocalDate,
                                           timeMessageGenerated: LocalTime,
                                           dateMessageLogged: LocalDate,
                                           timeMessageLogged: LocalTime,
                                           altitude: Int,
                                           squawkChanged: String,
                                           transponderIdent: String,
                                           isOnGround: isOnGroundType
                                         ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - Surveillance ID Message (MSG6)
    */
  final case class SurveillanceIdMessage(
                                          sessionId: String,
                                          aircraftId: String,
                                          hexIndent: String,
                                          flightId: String,
                                          dateMessageGenerated: LocalDate,
                                          timeMessageGenerated: LocalTime,
                                          dateMessageLogged: LocalDate,
                                          timeMessageLogged: LocalTime,
                                          altitude: Int,
                                          squawk: String,
                                          squawkChanged: String,
                                          emergency: String,
                                          transponderIdent: String,
                                          isOnGround: isOnGroundType
                                        ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - Air To Air Message (MSG7)
    */
  final case class AirToAirMessage(
                                    sessionId: String,
                                    aircraftId: String,
                                    hexIndent: String,
                                    flightId: String,
                                    dateMessageGenerated: LocalDate,
                                    timeMessageGenerated: LocalTime,
                                    dateMessageLogged: LocalDate,
                                    timeMessageLogged: LocalTime,
                                    altitude: Int,
                                    isOnGround: isOnGroundType
                                  ) extends Message with TransmissionMessage

  /**
    *  Transmission Message - All Call Reply (MSG8)
    */
  final case class AllCallReplyMessage(
                                        sessionId: String,
                                        aircraftId: String,
                                        hexIndent: String,
                                        flightId: String,
                                        dateMessageGenerated: LocalDate,
                                        timeMessageGenerated: LocalTime,
                                        dateMessageLogged: LocalDate,
                                        timeMessageLogged: LocalTime,
                                        isOnGround: isOnGroundType
                                      ) extends Message with TransmissionMessage

}