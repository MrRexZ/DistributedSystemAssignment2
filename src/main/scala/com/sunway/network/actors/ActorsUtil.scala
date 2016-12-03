package com.sunway.network.actors

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.sunway.model.Database._
import com.sunway.model.User._
import com.sunway.network.actors.MenuActorMessages.{BeAskedStats, BeAskedUsername}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._

/**
  * Created by Mr_RexZ on 11/25/2016.
  */
object ActorsUtil {
  def updateMembersNameList(newMembersList: List[Option[ActorRef]]): Unit = {
    implicit val timeout = Timeout(5 seconds)
    for (index <- 0 until newMembersList.size) {
      if (!newMembersList(index).isEmpty) {
        val futureName: Future[String] = (newMembersList(index).get ? BeAskedUsername).mapTo[String]
        futureName.onSuccess {
          case name => {
            playerNames(index).string = name
          }
        }
        val futureStats: Future[String] = (newMembersList(index).get ? BeAskedStats(index)).mapTo[String]
        futureStats.onSuccess {
          case stats => {
            playerRoomStats(index).string = stats
          }
        }
      }
      else {
        playerNames(index).string = ""
        playerRoomStats(index).string = ""
      }
    }
  }

  def allMembersReady(roomNum: Int): Boolean = {
    for ((client, index) <- roomActorRefPair(roomNum).zipWithIndex) {
      if (!client.isEmpty && clientRoomState(roomNum)(index) == WAITING_STATE) return false
    }
    true
  }

  def sendMessageToAllMembers[T](message: T, roomNum: Int): Unit = {
    for (client <- roomActorRefPair.get(roomNum).get
         if !client.isEmpty) {
      client.get ! message
    }
  }

  def sendMessagesToAllClientsNotMe[T](message: T, listPlayer: List[Option[ActorRef]], clientRef: ActorRef): Unit = {
    implicit val timeout = Timeout(5 seconds)
    for (player <- listPlayer
         if !player.isEmpty && !player.get.equals(clientRef))
      player.get ! message
  }



}
