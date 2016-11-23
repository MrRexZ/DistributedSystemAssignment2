package com.sunway.network.actors.testactors

import akka.actor.{ActorSystem, ExtendedActorSystem}
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by Mr_RexZ on 11/19/2016.
  */
object ActorServerTest extends App {

  val system: ActorSystem = ActorSystem("ClientSystem", clientConfig)
  val systessm: ExtendedActorSystem = system.asInstanceOf[ExtendedActorSystem]

  //  println( " YOW : "+ ConfigFactory.load(clientConfig).getString("akka.remote.netty.tcp.port"))
  var clientConfig: Config = ConfigFactory.parseString("akka.remote.netty.tcp.port = 0")
    .withFallback(ConfigFactory.parseString("akka.remote.netty.tcp.hostname = localhost"))
    .withFallback(ConfigFactory.parseString("akka.actor.provider = akka.remote.RemoteActorRefProvider"))
    .withFallback(ConfigFactory.load("common"));
  println(systessm.provider.getDefaultAddress.hostPort)

}
