/*
 * Copyright (C) 2016-2020 Lightbend Inc. <https://www.lightbend.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectedcar.streamlets

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import cloudflow.streamlets._
import cloudflow.streamlets.avro._
import cloudflow.spark.{SparkStreamlet, SparkStreamletLogic}
import org.apache.spark.sql.streaming.OutputMode
import cloudflow.spark.sql.SQLImplicits._
import connectedcar.data.{AggregatedMessageStats, ConnectedCarERecord}
import org.apache.log4j.{Level, Logger}

class ConnectedCarAggregator extends SparkStreamlet {

  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)

  val in    = AvroInlet[ConnectedCarERecord]("in")
  val out   = AvroOutlet[AggregatedMessageStats]("out")
  val shape: StreamletShape = StreamletShape(in, out)

  override def createLogic = new SparkStreamletLogic {

    override def buildStreamingQueries = {
      val dataset   = readStream(in)
      val outStream = process(dataset)
      writeStream(outStream, out, OutputMode.Update).toQueryExecution
    }

    private def process(inDataset: Dataset[ConnectedCarERecord]): Dataset[AggregatedMessageStats] = {
      val query =
        inDataset
          .withColumn("ts", $"timestamp".cast(TimestampType))
          .withWatermark("ts", s"100 milliseconds")
          .groupBy(window($"ts", s"1 second"))
          .agg(count("*").as("numberOfMessages"))
          .withColumn("windowDuration", $"window.end".cast(LongType) - $"window.start".cast(LongType))

      query
        .select($"window.start".cast(LongType).as("startTime"), $"windowDuration", $"numberOfMessages")
        .as[AggregatedMessageStats]
    }
  }
}
