package connectedcar.streamlets

import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.util.Timeout
import cloudflow.akkastream.scaladsl.{FlowWithCommittableContext, RunnableGraphStreamletLogic}
import cloudflow.streamlets.StreamletShape
import cloudflow.streamlets.avro.{AvroInlet}
import connectedcar.actors.{Ack, ConnectedCarActor, ConnectedCarERecordWrapper}

import scala.concurrent.duration._
import cloudflow.akkastream.{AkkaStreamlet, Clustering}
import connectedcar.data.{AggregatedMessageStats, ConnectedCarERecord}

class ConnectedCarCluster extends AkkaStreamlet with Clustering {
  val msgin    = AvroInlet[ConnectedCarERecord]("msg-in")
  val aggin    = AvroInlet[AggregatedMessageStats]("agg-in")
  val shape: StreamletShape = StreamletShape.withInlets(msgin, aggin)

  override def createLogic = new RunnableGraphStreamletLogic() {
    val typeKey = EntityTypeKey[ConnectedCarERecordWrapper]("Car")
    val sharding = clusterSharding()
    sharding.init(Entity(typeKey)(createBehavior = entityContext => ConnectedCarActor(entityContext.entityId)))

    def runnableGraph = sourceWithCommittableContext(msgin).via(flow).to(committableSink)

    implicit val timeout: Timeout = 3.seconds
    def flow =
      FlowWithCommittableContext[ConnectedCarERecord]
        .mapAsync(5)(msg ⇒ {
            val carActor = sharding.entityRefFor(typeKey, msg.carId.toString)
            carActor.ask[Ack](ref => ConnectedCarERecordWrapper(msg, ref))
          })
  }
}
