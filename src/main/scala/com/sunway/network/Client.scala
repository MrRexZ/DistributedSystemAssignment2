package com.sunway.network

import akka.actor.{ActorRef, ActorSystem, ExtendedActorSystem, Identify, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.github.dunnololda.scage.ScageScreenApp
import com.sunway.network.actors.MenuActorMessages._
import com.sunway.network.actors.{ClientActor, SupervisorActor}
import com.sunway.screen.menu.MainMenu
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


/**
  * Created by Mr_RexZ on 11/18/2016.
  */


//TODO Assign each client with unique port number
object Client extends ScageScreenApp("Client App", 640, 480) {
  implicit val timeout = Timeout(5.seconds)

  val clientSystem = ActorSystem("ClientSystem", ConfigFactory.load("client"))

  println(clientSystem.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress.port)

  val serverPath = "akka.tcp://ServerSystem@127.0.0.1:2554/user/serverActorName"
  val actorServerSelect = clientSystem.actorSelection(serverPath)

  //TODO identify message should be sent only when there is a process required
  actorServerSelect ! Identify(GET_REF_SERVER)

  var supervisorActor = clientSystem.actorOf(Props(classOf[SupervisorActor]), name = "Supervisor")
  var clientActor: ActorRef = null
  createClientActor()
  MainMenu.run()

  def createClientActor(): Unit = {
    var futureClientActor = (supervisorActor ? Props(classOf[ClientActor])).mapTo[ActorRef]
    futureClientActor onSuccess {
      case clientRef => clientActor = clientRef
    }

  }


}
