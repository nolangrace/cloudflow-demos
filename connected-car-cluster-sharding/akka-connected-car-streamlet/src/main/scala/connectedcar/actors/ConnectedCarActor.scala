package connectedcar.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import connectedcar.data.{AggregatedMessageStats, ConnectedCarAgg, ConnectedCarERecord}

case class Ack()

sealed trait ConnectedCarMsg
case class ConnectedCarERecordWrapper(record: ConnectedCarERecord, sender:ActorRef[Ack]) extends ConnectedCarMsg
case class ConnectedCarAggWrapper(record: AggregatedMessageStats, sender:ActorRef[Ack]) extends ConnectedCarMsg
case class GetCarDetails(sender: ActorRef[CarDetails]) extends ConnectedCarMsg

case class CarDetails(numberOfRecords: Int, driverName: String, carId:String, averageSpeed: Double, currentSpeed: Double)

object ConnectedCarActor {
  def apply(carId:String): Behavior[ConnectedCarMsg] = {
    def updated(numberOfRecords: Int, driverName: String, carId:String, averageSpeed: Double, currentSpeed: Double): Behavior[ConnectedCarMsg] = {
      Behaviors.receive { (context, message) => {
        message match {
            case ConnectedCarERecordWrapper(record, sender) =>
              context.log.info ("Update Received- CarId: " + carId + " MessageCarId: " + record.carId +
              " From Actor:" + sender.path)

              val newAverage = ((averageSpeed * numberOfRecords) + record.speed) / (numberOfRecords + 1)
              val newNumberOfRecords = numberOfRecords + 1

              val newAgg = ConnectedCarAgg (record.carId, record.driver, newAverage, newNumberOfRecords)

              sender ! Ack()

              updated (newNumberOfRecords, record.driver, carId, newAverage, record.speed)
            case ConnectedCarAggWrapper(record, sender) =>
              context.log.info ("Agg Update Received- CarId: " + carId)
              updated(numberOfRecords, driverName, carId, averageSpeed, currentSpeed)
            case GetCarDetails(sender) =>{
              sender ! CarDetails(numberOfRecords, driverName, carId, averageSpeed, currentSpeed)

              Behaviors.same
            }
          }
        }
      }
    }

    updated(0, "", carId, 0, 0.0)
  }
}
