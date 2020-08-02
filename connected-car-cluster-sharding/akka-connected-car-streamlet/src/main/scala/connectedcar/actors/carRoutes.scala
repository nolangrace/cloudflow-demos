package connectedcar.actors

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityTypeKey}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Complete
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class CarRoutes()(implicit val system: ActorSystem[_]) {

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout:Timeout = 5.seconds
  val sharding = ClusterSharding(system)
  val typeKey = EntityTypeKey[ConnectedCarMsg]("Car")

//  def getUsers(): Future[Users] =
//    userRegistry.ask(GetUsers)
  def getCar(id: String): Future[CarDetails] = {
    val carActor = sharding.entityRefFor(typeKey, id)
    carActor.ask[CarDetails](ref => GetCarDetails(ref))
  }

  //#all-routes
  //#users-get-post
  //#users-get-delete
  val carRoutes: Route =
    path("order" / Remaining) { carId =>
      get {
        complete {
          println("Http Received for: "+carId)
          Await.result(getCar(carId), 5.seconds).toString
        }
      }
    }
  //#all-routes
}
