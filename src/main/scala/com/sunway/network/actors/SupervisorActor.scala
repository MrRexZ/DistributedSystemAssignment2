package com.sunway.network.actors

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorKilledException, OneForOneStrategy, Props}

import scala.concurrent.duration._

/**
  * Created by Mr_RexZ on 11/25/2016.
  */
class SupervisorActor extends Actor {

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: ActorKilledException => Stop
      case _: Exception => {
        println("RESTARTING PARENT FROM SUPERVISOR")
        Restart
      }
    }

  override def preStart =
    println("STARTED SUPERVISOR")

  override def postStop(): Unit =
    println("SUPERVISOR POSTSTOP")

  def receive = {
    case p: Props => sender() ! context.actorOf(p)
  }

}