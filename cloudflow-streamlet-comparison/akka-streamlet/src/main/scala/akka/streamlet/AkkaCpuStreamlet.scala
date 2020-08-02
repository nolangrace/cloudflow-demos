package akka.streamlet

import java.util.UUID

import akka.stream.scaladsl.{Sink, Source}
import cloudflow.akkastream.AkkaStreamlet
import cloudflow.akkastream.scaladsl.RunnableGraphStreamletLogic
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.AvroInlet
import resource.consumption.CpuConsumer.calculatePrimes
import streamletTest.data.TestEvent

import scala.concurrent.duration._

class AkkaCpuStreamlet extends AkkaStreamlet {
  val in = AvroInlet[TestEvent]("in")
  val shape: StreamletShape = StreamletShape(in)

  override def createLogic = new RunnableGraphStreamletLogic() {
    def runnableGraph = sourceWithCommittableContext(in)
      .map(x => {
        calculatePrimes()
      })
      .to(Sink.ignore)
  }

}
