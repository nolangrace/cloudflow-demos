package spark.streamlet

import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic}
import cloudflow.spark.sql.SQLImplicits._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.DataFrame
import resource.consumption.CpuConsumer.calculatePrimes
import streamletTest.data.TestEvent

class SparkCpuStreamlet extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in    = AvroInlet[TestEvent]("in")
  val shape: StreamletShape = StreamletShape(in)

  override def createLogic = new SparkStreamletLogic {
    override def buildStreamingQueries =
      readStream(in)
        .map(x => {
          calculatePrimes()
          x
        })
        .writeStream
        .format("console")
        .start()
        .toQueryExecution
  }
}
