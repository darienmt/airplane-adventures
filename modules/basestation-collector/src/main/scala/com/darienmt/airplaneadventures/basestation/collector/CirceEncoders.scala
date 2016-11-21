package com.darienmt.airplaneadventures.basestation.collector

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import io.circe.{Decoder, Encoder}

object CirceEncoders {
  import io.circe.java8.time.{encodeLocalTime, decodeLocalTime}

  implicit final val encodeLocalTimeDefault: Encoder[LocalTime] =
    encodeLocalTime(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))

  implicit final val decodeLocalTimeDefault: Decoder[LocalTime] =
    decodeLocalTime(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
}
