package com.darienmt.airplaneadventures.basestation.collector

import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka
import com.darienmt.airplaneadventures.basestation.collector.streams.BaseStation2Kafka.{ SinkConfig, SourceConfig }
import com.darienmt.keepers.{ Generator, KeepThisUp, MainCommons }

object Main extends App with MainCommons {

  val sourceConfig = SourceConfig(
    config.getString("station.address"),
    config.getInt("station.port")
  )

  val sinkConfig = SinkConfig(
    config.getString("kafka.address"),
    config.getInt("kafka.port"),
    config.getString("kafka.topic")
  )

  val generator: Generator = () => BaseStation2Kafka(sourceConfig, sinkConfig)

  val keeper = KeepThisUp(config)
  keeper(generator)
}
