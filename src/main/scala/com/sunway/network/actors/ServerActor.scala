package com.sunway.network.actors


import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.Database._
import com.sunway.model.User._
import com.sunway.model.{ConfigurationObject, LevelGenerator}
import com.sunway.network.Server._
import com.sunway.network.actors.GameplayActorMessages.DistributeMessageToAllClients
import com.sunway.network.actors.MenuActorMessages._

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}
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
        clientRoomState.put(roomNum, ListBuffer(WAITING_STATE, WAITING_STATE))
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

            updateClientsList(currentRoomMembers, actorRef)
            heartBeatActorRef.get(roomNum).get ! HeartbeatMessage(myRoomPos)
            clientRoomState(roomNum).update(myRoomPos, WAITING_STATE)
            sender ! AcceptPlayerAsParticipant(roomNum, myRoomPos, currentRoomMembers)

          }
          case _ => sender ! RejectPlayer("Room is full ! ")
        }
      }
    }

    case AskNumOfParticipants(roomNum, clientRef) => {
      sender ! containsClient(roomNum, clientRef)
    }

    case SendRoomState(clientRef: ActorRef, roomNum: Int, roomPos: Int, playerRoomState: Int, text: String) => {
      clientRoomState(roomNum).update(roomPos, playerRoomState)
      implicit val timeout = Timeout(5.seconds)
      val askTargetName: Future[String] = (clientRef ? BeAskedUsername).mapTo[String]

      askTargetName onSuccess {
        case targetName => {
          for (client <- roomActorRefPair(roomNum)
               if !client.isEmpty) {
            client.get ! UpdatePlayerTextState(roomPos, text)
          }
        }
      }

      println("all members ready " + allMembersReady)

      if (allMembersReady) {
        roomIsPlaying.put(roomNum, true)
        sendMessageToAllMembers(StartGame(roomActorRefPair(roomNum)), roomNum)
        val levelObject = new LevelGenerator(roomNum, roomActorRefPair(roomNum))
        levelObject.genLevel(0, 3, Vec(0, ConfigurationObject.windowHeight / 2 - 100), 0, 1000, ConfigurationObject.windowHeight / 2 - 100)
        levelObject.sendGeneratedMap()
      }

      def allMembersReady: Boolean = {
        for ((client, index) <- roomActorRefPair(roomNum).zipWithIndex) {
          if (client.isEmpty) return false
          else {
            if (clientRoomState(roomNum)(index) == WAITING_STATE) return false
          }
        }
        true
      }
    }


    case BeAskedRoomIsPlaying(roomNum) => sender ! roomIsPlaying(roomNum)

    //TODO remove this on the win events
    case DistributeMessageToAllClients(message, roomNum) => {
      sendMessageToAllMembers(message, roomNum)
    }


    case AllPlayerReceivedMap(roomNum) => {
      val tempMapState = Array.fill[Int](maxPlayerInRoom)(WAITING_STATE)
      val tempClientsList = roomActorRefPair(roomNum)
      for ((clientRefOpt, i) <- roomActorRefPair(roomNum).zipWithIndex) {
        (clientRefOpt.get ? BeAskedMapState).mapTo[Int].onComplete {
          case Success(state) => {
            tempMapState(i) = state
            if (allMapReadyState(tempMapState)) {
              for (clientRef <- tempClientsList) {
                clientRef.get ! BeAskedPlay
              }
            }
          }
          case Failure(fail) => println("Failed in asking player map state")
        }
      }
    }
  }

  def allMapReadyState(tempMapState: Array[Int]): Boolean = {
    for (state <- tempMapState) {
      if (state == WAITING_STATE) return false
    }
    return true
  }


  def containsRoom(roomNum: Int): Boolean = roomNumList.contains(roomNum.toInt)

  def validUsers(userName: String, password: String): Boolean = {
    userNameToPassword.get(userName).get.equals(password)
  }

  def checkRoomEmptySlot(roomNum: Int, actorRef: ActorRef): Option[Int] = {

    val roomPlayerList = roomActorRefPair.get(roomNum).get
    var counter = 0;
    for (playerSlot <- roomPlayerList) {
      if (playerSlot.isEmpty) {

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


  def updateClientsList(currentRoomMembers: List[Option[ActorRef]], joiningClient: ActorRef) = {
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
