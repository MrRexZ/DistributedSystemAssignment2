package com.sunway.network.actors

import akka.actor.{Actor, ActorIdentity, ActorRef, Cancellable, Identify, Props}
import com.sunway.model.User._
import com.sunway.network.Server._
import com.sunway.network.actors.ActorMessages.{FrequencyChangeMessage, HeartbeatMessage, StartMessage, _}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Mr_RexZ on 11/19/2016.
  */

class HeartbeatActor(roomNum: Int, interval: Int, clientActors: ListBuffer[Option[ActorRef]]) extends Actor {


  var schedulerList = Array.ofDim[Cancellable](4)
  var newListBuffer = clientActors

  //TODO Implement several states here if possible
  override def receive: Receive = receive(interval)

  override def preStart() {
    self ! StartMessage
  }

  def receive(interval: Int): Actor.Receive = {
    case StartMessage => context.system.scheduler.scheduleOnce(interval.milliseconds, self, HeartbeatMessage(HOST_ROOM_ID))
    case HeartbeatMessage(playerID) => {
      //TODO send to all actor ref opponents
      //  context.system.scheduler.scheduleOnce(interval.milliseconds, newListBuffer(playerID).get, Identify(playerID))
      schedulerList(playerID) = context.system.scheduler.schedule(0.millisecond, interval.milliseconds, newListBuffer(playerID).get, Identify(playerID))
      // println("CLIENT ACTORS : " + playerID)

    }

    case FrequencyChangeMessage(interval, clientActors) => context.become(receive(interval))


    case ActorIdentity(playerID, Some(actorRef)) => {
      // context.system.scheduler.scheduleOnce(interval.milliseconds, self, HeartbeatMessage(playerID.toString.toInt))
      //  println("CLIENT ACTORS : " + playerID)
    }


    case ActorIdentity(playerID, None) => {
      println("REMOVING ACTOR : " + playerID.toString.toInt + " and " + newListBuffer)
      schedulerList(playerID.toString.toInt).cancel()
      removeActor
      if (allActorsDead) context.stop(self)


      def allActorsDead: Boolean = {
        for (clientActor <- newListBuffer) {
          if (!clientActor.isEmpty) return false
        }
        return true
      }

      //TODO remove the display from player
      def removeActor {
        val removedPlayer: Int = playerID.toString.toInt
        var newListActors: ListBuffer[Option[ActorRef]] = newListBuffer
        newListActors.update(removedPlayer, None)
        updateClientsList(newListActors)
        self ! UpdateClientsList(newListActors)
        serverActor ! UpdateRoomServerList(roomNum, newListActors)

      }
    }

    case UpdateClientsList(newList) => {
      newListBuffer = newList
    }
    case _ => println("wut?\n")

  }

  def updateClientsList(newListActors: ListBuffer[Option[ActorRef]]): Unit = {
    for (clientActor <- newListActors
         if !clientActor.isEmpty) {
      clientActor.get ! UpdateClientsList(newListActors)
    }
  }

}

object HeartbeatActor {

  def props(roomNum: Int, interval: Int, clientActors: ListBuffer[Option[ActorRef]]): Props = Props(new HeartbeatActor(roomNum, interval, clientActors))
}
