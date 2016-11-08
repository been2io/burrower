package com.github.splee.burrower.write

import com.github.splee.burrower.lag.LagGroup
import com.paulgoldbaum.influxdbclient.{InfluxDB, Point}
import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.typesafe.scalalogging.LazyLogging
import scala.compat.Platform._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success}

class InfluxWriter(
  influxHost: String="localhost",
  influxPort: Int=8086,
  influxDatabase: String="burrower",
  influxSeries: String="kafka_consumer_lag",
  userName: String="",
  password: String=""
) extends Writer with LazyLogging {

  val influxdb = InfluxDB.connect(influxHost, influxPort, userName, password)
  val database = influxdb.selectDatabase(influxDatabase)

  def write(lagGroup: LagGroup): Unit = {
    val points = lagGroup.lags.map(lag => {
      Point(influxSeries, lagGroup.timestamp)
        .addTag("cluster", lag.cluster)
        .addTag("consumer_group", lag.group)
        .addTag("topic", lag.topic)
        .addTag("partition", lag.partition.toString)
        .addField("offset", lag.offset)
        .addField("lag", lag.lag)
    })
    database.bulkWrite(points, precision = Precision.MILLISECONDS)
      .onComplete(_ match {
        case Success(v) =>
          logger.debug("Metrics sent to InfluxDB")
        case Failure(e) =>
          logger.error(f"Sending metrics to InfluxDB failed: ${e.getMessage}")
      })
  }
}
