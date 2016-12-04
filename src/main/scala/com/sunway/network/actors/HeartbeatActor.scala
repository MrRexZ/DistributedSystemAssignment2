package com.sunway.network.actors

import akka.actor.{Actor, ActorIdentity, ActorRef, Cancellable, Identify, Props}
import com.sunway.model.Database._
import com.sunway.model.User._
import com.sunway.network.actors.ActorsUtil._
import com.sunway.network.actors.GameplayActorMessages.UpdateClientsListRemovePlayerInGame
import com.sunway.network.actors.MenuActorMessages.{FrequencyChangeMessage, HeartbeatMessage, StartMessage, _}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by Mr_RexZ on 11/19/2016.
  */

class HeartbeatActor(roomNum: Int, interval: Int, clientActors: ListBuffer[Option[ActorRef]]) extends Actor {

  var schedulerList = Array.ofDim[Cancellable](maxPlayerInRoom)

  override def receive: Receive = receive(interval)

  override def preStart() {
    self ! StartMessage
  }

  def receive(interval: Int): Actor.Receive = {
    case StartMessage => context.system.scheduler.scheduleOnce(interval.milliseconds, self, HeartbeatMessage(HOST_ROOM_ID))
    case HeartbeatMessage(playerID) => {
      schedulerList(playerID) = context.system.scheduler.schedule(0.millisecond, interval.milliseconds, clientActors(playerID).get, Identify(playerID))
    }

    case FrequencyChangeMessage(interval, clientActors) => context.become(receive(interval))


    case ActorIdentity(playerID, Some(actorRef)) => {
      //  println("AFTER UPDATE : " + Database.roomActorRefPair)
    }


    case ActorIdentity(playerID, None) => {
      println("REMOVING ACTOR : " + playerID.toString.toInt + " and " + clientActors(playerID.toString.toInt).get)
      schedulerList(playerID.toString.toInt).cancel()
      removeActor

      //TODO ACTIVATE THIS LINE BELOW LATER
      //if (allActorsDead) context.stop(self)


      def allActorsDead: Boolean = {
        for (clientActor <- clientActors) {
          if (!clientActor.isEmpty) return false
        }
        return true
      }

      def removeActor {
        val removedPlayer: Int = playerID.toString.toInt
        clientActors.update(removedPlayer, None)
        if (!roomIsPlaying.get(roomNum).isEmpty) sendMessageToAllMembers(UpdateClientsListRemovePlayerInGame(clientActors, removedPlayer), roomNum)
        else sendMessageToAllMembers(UpdateClientsList(clientActors), roomNum)

      }
    }

    case _ => println("what?\n")

  }


}

object HeartbeatActor {

  def props(roomNum: Int, interval: Int, clientActors: ListBuffer[Option[ActorRef]]): Props = Props(new HeartbeatActor(roomNum, interval, clientActors))
}
