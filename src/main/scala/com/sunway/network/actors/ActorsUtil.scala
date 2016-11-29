package com.sunway.network.actors

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
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


}
