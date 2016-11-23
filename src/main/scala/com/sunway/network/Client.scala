package com.sunway.network

import akka.actor.{ActorSystem, ExtendedActorSystem, Identify, Props}
import com.github.dunnololda.scage.ScageScreenApp
import com.sunway.network.actors.ActorMessages._
import com.sunway.network.actors.ClientActor
import com.sunway.screen.menu.MainMenu
import com.typesafe.config.ConfigFactory

/**
  * Created by Mr_RexZ on 11/18/2016.
  */


//TODO Assign each client with unique port number
object Client extends ScageScreenApp("Client App", 640, 480) {


  val clientSystem = ActorSystem("ClientSystem", ConfigFactory.load("client"))

  println(clientSystem.asInstanceOf[ExtendedActorSystem].provider.getDefaultAddress.port)

  val serverPath = "akka.tcp://ServerSystem@127.0.0.1:2553/user/serverActorName"
  val actorServerSelect = clientSystem.actorSelection(serverPath)

  //TODO identify message should be sent only when there is a process required
  actorServerSelect ! Identify(GET_REF_SERVER)

  val clientActor = clientSystem.actorOf(Props(classOf[ClientActor]), "clientActorName")

  // val ref = system.actorOf(Props[SampleActor].
  // withDeploy(Deploy(scope = RemoteScope(address))))

  MainMenu.run()


}
