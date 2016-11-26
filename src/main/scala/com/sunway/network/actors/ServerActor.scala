package com.sunway.network.actors


import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.sunway.model.Database._
import com.sunway.model.User._
import com.sunway.network.Server._
import com.sunway.network.actors.GameplayActorMessages.DistributeMessageToAllClients
import com.sunway.network.actors.MenuActorMessages._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
/**
  * Created by Mr_RexZ on 11/18/2016.
  */
class ServerActor extends Actor {



  def receive = {
    case SendRequestCreateRoom(actorRef, userName, password) => {


      if (!userNameToPassword.contains(userName)) registerPlayer(userName, password, actorRef)
      if (!validUsers(userName, password)) sender ! RejectPlayer(" WRONG PASSWORD ")
      else {
        //TODO uncomment this line later!!
        // val roomNum = generateRoomNum().toString
        val roomNum = 0
        registerRoom(roomNum, actorRef)
        val currentRoomList = roomActorRefPair(roomNum)

        val heartbeatActor = serverSystem.actorOf(HeartbeatActor.props(roomNum, 500, currentRoomList), name = roomNum.toString)
        heartBeatActorRef.put(roomNum, heartbeatActor)
        sender ! AcceptPlayerAsHost(roomNum, HOST_ROOM_ID, currentRoomList.toList)

        println("Player received")
      }



      def registerRoom(roomNum: Int, clientRef: ActorRef) {
        roomNumList += roomNum
        roomActorRefPair.put(roomNum, ListBuffer(Some(actorRef), None))
        clientRoomState.put(clientRef, WAITING_STATE)
      }

    }


    //TODO send actorref to existing people too in here
    case SendRequestJoin(actorRef, roomNum, userName, password) => {

      if (!userNameToPassword.contains(userName)) registerPlayer(userName, password, actorRef)
      if (!validUsers(userName, password)) sender ! RejectPlayer(" wrong password")
      else if (!containsRoom(roomNum)) sender ! RejectPlayer(" no such room is available")
      else {
        checkRoomEmptySlot(roomNum.toInt, actorRef) match {
          case Some(myRoomPos) => {

            val currentRoomMembers = getRoomMembers(roomNum)

            updateClientAndHeartbeat(currentRoomMembers, actorRef, roomNum)
            heartBeatActorRef.get(roomNum).get ! HeartbeatMessage(myRoomPos)
            clientRoomState.put(actorRef, WAITING_STATE)
            sender ! AcceptPlayerAsParticipant(roomNum, myRoomPos, currentRoomMembers)

          }
          case _ => sender ! RejectPlayer("Room is full ! ")
        }
      }
    }
    case UpdateRoomServerList(roomNum, newListActors) => {
      updateRoomServerList(roomNum, newListActors)
    }

    case AskNumOfParticipants(roomNum, clientRef) => {
      sender ! containsClient(roomNum, clientRef)
    }

    case SendRoomState(clientRef: ActorRef, roomNum: Int, roomPos: Int, playerRoomState: Int, text: String) => {
      clientRoomState.update(clientRef, playerRoomState)
      implicit val timeout = Timeout(5.seconds)
      val askTargetName: Future[String] = (clientRef ? BeAskedUsername).mapTo[String]

      askTargetName onSuccess {
        case targetName => {
          for (client <- roomActorRefPair(roomNum)
               if !client.isEmpty) {
            client.get ! UpdatePlayerTextState(roomPos, targetName, text)
          }
        }

      }

      //TODO WARNING HERE, DOES THE CLASS CORRECTLY DETECT THE MESSAGE?
      if (allMembersReady) sendMessageToAllMembers(StartGame(roomActorRefPair(roomNum)), roomNum)

      def allMembersReady: Boolean = {
        for (client <- roomActorRefPair(roomNum)) {
          if (client.isEmpty) return false
          else {
            if (clientRoomState.get(client.get).get == WAITING_STATE) return false
          }
        }
        true
      }
    }



    //TODO remove this on the win events
    case DistributeMessageToAllClients(message, roomNum) => {
      sendMessageToAllMembers(message, roomNum)
    }

  }

  def containsRoom(roomNum: Int): Boolean = roomNumList.contains(roomNum.toInt)

  def validUsers(userName: String, password: String): Boolean = {
    userNameToPassword.get(userName).get.equals(password)
  }

  def checkRoomEmptySlot(roomNum: Int, actorRef: ActorRef): Option[Int] = {
    // val roomPlayerListImmutable : java.util.List[Option[ActorRef]] = roomToActorRefPair.get(roomNum.toString)
    //   var roomPlayerList = roomPlayerListImmutable.toArray(new Array[Option[ActorRef]](roomPlayerListImmutable.size()))

    val roomPlayerList = roomActorRefPair.get(roomNum).get
    var counter = 0;
    for (playerSlot <- roomPlayerList) {
      if (playerSlot.isEmpty) {

        //   val newList = updatedRoomActorRef(roomNum, counter, Some(actorRef))
        //   roomActorRefPair.update(roomNum,newList)

        roomActorRefPair(roomNum).update(counter, Some(actorRef))
        println("Current ListBuffer state : " + roomActorRefPair.get(roomNum).get)
        return Some(counter)
      }
      counter += 1
    }


    return None
  }


  def registerPlayer(userName: String, password: String, actorRef: ActorRef): Unit = {
    if (password.length >= 0) userNameToPassword += (userName -> password)
    else sender ! RejectPlayer(" you didn't insert a password")
  }

  def updateRoomServerList(roomNum: Int, newRoomList: ListBuffer[Option[ActorRef]]) = {
    // roomActorRefPair.update(roomNum, newRoomList)
  }

  def updateClientAndHeartbeat(currentRoomMembers: List[Option[ActorRef]], joiningClient: ActorRef, roomNum: Int) = {

    updateClientsList(currentRoomMembers, joiningClient)
    //   updateHeartbeatList(currentRoomMembers, roomNum)
  }

  def updateClientsList(currentRoomMembers: List[Option[ActorRef]], joiningClient: ActorRef) = {
    sendClientsUpdatedWaitingList(currentRoomMembers, joiningClient)
  }

  def sendClientsUpdatedWaitingList(currentRoomMembers: List[Option[ActorRef]], joiningClient: ActorRef) = {
    for (otherClients <- currentRoomMembers
         if (!otherClients.isEmpty && !otherClients.get.equals(joiningClient))) {
      otherClients.get ! UpdateClientsList(currentRoomMembers.to[ListBuffer])
    }
  }

  def getRoomMembers(roomNum: Int) = {
    roomActorRefPair.get(roomNum).get.toList
  }

  def containsClient(roomNum: Int, clientRef: ActorRef): Boolean = {
    for (client <- getRoomMembers(roomNum)) {
      if (client.get.equals(clientRef)) return true
    }
    return false
  }

  def sendMessageToAllMembers[T](message: T, roomNum: Int): Unit = {
    for (client <- roomActorRefPair.get(roomNum).get
         if !client.isEmpty) {
      client.get ! message
    }
  }

  def updateHeartbeatList(currentRoomMembers: List[Option[ActorRef]], roomNum: Int) = {
    heartBeatActorRef.get(roomNum).get ! UpdateClientsList(currentRoomMembers.to[ListBuffer])
  }

  def generateRoomNum(): Int = {
    var genRoom: Int = Random.nextInt(1000)
    while (roomNumList.contains(genRoom)) {
      genRoom = Random.nextInt(1000)
    }
    genRoom
  }

  /*
  def sendMessageToAllMembers(roomNum : Int)(message : => Unit): Unit = {
    for (client <- roomActorRefPair.get(roomNum).get
         if !client.isEmpty) {
              client.get ! message
    }

  }

*/



}
