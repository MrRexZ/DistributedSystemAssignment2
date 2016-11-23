package com.sunway.network.actors

import akka.actor.{Actor, ActorIdentity, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.sunway.model.User._
import com.sunway.network.actors.ActorMessages._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by Mr_RexZ on 11/18/2016.
  */


class ClientActor extends Actor {
  var actorServerRef: Option[ActorRef] = None;
  var inRoom: Boolean = false
  var latestServerReply: String = ""

  def receive = {

    case ActorIdentity(GET_REF_SERVER, Some(ref)) => actorServerRef = Some(ref)
    case AcceptPlayerAsHost(roomNum, playerRoomID, actorRefList) => {

      latestServerReply = myName + ", you're ACCEPTED as HOST, room num is " + roomNum + "and ROOM ID is " + playerRoomID
      println(latestServerReply)
      assignNameToRoom(roomNum, playerRoomID, actorRefList)

    }

    case AcceptPlayerAsParticipant(roomNum, playerRoomID, actorRefList) => {
      latestServerReply = myName + ", you're ACCEPTED as PARTICIPANT, room num is " + roomNum + "and ROOM ID is " + playerRoomID
      println(latestServerReply)
      assignNameToRoom(roomNum, playerRoomID, actorRefList)
    }
    case RejectPlayer(message) => println(myName + ", you're rejected because" + message)

    case BeAskedUsername => sender ! myName.string

    case UpdateClientsList(roomMembers) => {
      updateMembersList(roomMembers.toList)
    }
    case _ => println("MESSAGE UNKNOWN")
  }

  def assignNameToRoom(roomNum: Int, playerRoomID: Int, actorRefList: List[Option[ActorRef]]): Unit = {
    //    println("ACTOR REF SIZE "+ actorRefList(0).isEmpty + actorRefList(1).isEmpty + actorRefList(2).isEmpty +actorRefList(3).isEmpty)
    inRoom = true
    targetRoomNum.string = roomNum.toString
    updateMembersList(actorRefList)
    myRoomPos.string = playerRoomID.toString

  }

  def updateMembersList(newMembersList: List[Option[ActorRef]]): Unit = {
    implicit val timeout = Timeout(5 seconds)
    for (index <- 0 until newMembersList.size) {
      if (!newMembersList(index).isEmpty) {
        val futureName: Future[String] = (newMembersList(index).get ? BeAskedUsername).mapTo[String]
        futureName.onSuccess {
          case name => {
            playerNames(index).string = name
          }
        }
      }
      else playerNames(index).string = ""
    }
  }

  //TODO work on this receive method
  def beHost: Actor.Receive = {
    case NewParticipant => println("hehehe")
    case _ => println("MESSAGE NOT DETECTED")
  }

}
