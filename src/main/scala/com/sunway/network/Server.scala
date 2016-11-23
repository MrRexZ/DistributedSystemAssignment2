package com.sunway.network

import akka.actor.{ActorSystem, Props}
import com.sunway.network.actors.ServerActor
import com.typesafe.config.ConfigFactory

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object Server extends App {
  val serverSystem = ActorSystem("ServerSystem", ConfigFactory.load("server"))
  val serverActor = serverSystem.actorOf(Props(classOf[ServerActor]), "serverActorName")
}
