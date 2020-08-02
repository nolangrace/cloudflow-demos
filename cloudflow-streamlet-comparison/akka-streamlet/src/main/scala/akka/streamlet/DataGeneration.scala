package akka.streamlet

import java.util.UUID

import akka.stream.scaladsl.{Sink, Source}
import cloudflow.akkastream.AkkaStreamlet
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroOutlet
import streamletTest.data.TestEvent

import scala.concurrent.duration._

class DataGeneration extends AkkaStreamlet {
  val out   = AvroOutlet[TestEvent]("out")
  val shape = StreamletShape.withOutlets(out)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = Source.repeat(UUID.randomUUID())
      .throttle(1000, 1.second)
      .map(x => {
        TestEvent(x.toString)
      })
      .to(plainSink(out))
  }

}
