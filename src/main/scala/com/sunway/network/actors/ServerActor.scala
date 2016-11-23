package com.sunway.network.actors


import akka.actor.{Actor, ActorRef}
import com.google.common.collect.{BiMap, HashBiMap}
import com.sunway.model.User._
import com.sunway.network.Server._
import com.sunway.network.actors.ActorMessages._

import scala.collection.mutable.{ListBuffer, Map}
import scala.util.Random

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
class ServerActor extends Actor {


  //TODO change ActorRef to ActorSelection
  // var roomToActorRefPair : ArrayListMultimap[ String, Option[ActorRef]] = ArrayListMultimap.create()
  var roomActorRefPair = Map[Int, ListBuffer[Option[ActorRef]]]()


  //TODO Create an authentication system later on to validate username.
  var userNameToPassword = scala.collection.mutable.Map[String, String]()
  var heartBeatActorRef = Map[Int, ActorRef]()
  //TODO Resolve situation when name clashes happen in the BiMap
  var actorReftoUsername: BiMap[ActorRef, String] = HashBiMap.create()


  var roomNumList = Set[Integer]()

  def receive = {
    case SendRequestCreateRoom(actorRef, userName, password) => {


      if (!userNameToPassword.contains(userName)) registerPlayer(userName, password, actorRef)
      if (!validUsers(userName, password)) sender ! RejectPlayer(" WRONG PASSWORD ")
      else {


        //TODO uncomment this line later!!
        // val roomNum = generateRoomNum().toString
        val roomNum = 0
        registerRoom(roomNum)
        val immutableList = roomActorRefPair.get(roomNum).get.toList

        val heartbeatActor = serverSystem.actorOf(HeartbeatActor.props(roomNum, 500, immutableList.to[ListBuffer]), name = roomNum.toString)
        heartBeatActorRef.put(roomNum, heartbeatActor)
        sender ! AcceptPlayerAsHost(roomNum, HOST_ROOM_ID, immutableList)

        println("Player received")
      }



      def registerRoom(roomNum: Int) {
        roomNumList += roomNum
        roomActorRefPair.put(roomNum, ListBuffer(Some(actorRef), None))
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

        val newList = updatedRoomActorRef(roomNum, counter, Some(actorRef))
        roomActorRefPair += (roomNum -> newList)
        println("Current ListBuffer state : " + roomActorRefPair.get(roomNum).get)
        return Some(counter)
      }
      counter += 1
    }


    return None
  }

  def updatedRoomActorRef(roomNum: Int, posIndex: Int, newState: Option[ActorRef]) = {
    roomActorRefPair.get(roomNum).get.updated(posIndex, newState)
  }

  def registerPlayer(userName: String, password: String, actorRef: ActorRef): Unit = {
    if (password.length >= 0) userNameToPassword += (userName -> password)
    else sender ! RejectPlayer(" you didn't insert a password")
  }

  def updateRoomServerList(roomNum: Int, newRoomList: ListBuffer[Option[ActorRef]]) = {
    roomActorRefPair += (roomNum -> newRoomList)
  }

  def updateClientAndHeartbeat(currentRoomMembers: List[Option[ActorRef]], joiningClient: ActorRef, roomNum: Int) = {


    updateClientsList(currentRoomMembers, joiningClient)
    updateHeartbeatList(currentRoomMembers, roomNum)
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

  def updateHeartbeatList(currentRoomMembers: List[Option[ActorRef]], roomNum: Int) = {
    heartBeatActorRef.get(roomNum).get ! UpdateClientsList(currentRoomMembers.to[ListBuffer])
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

  def generateRoomNum(): Int = {
    var genRoom: Int = Random.nextInt(1000)
    while (roomNumList.contains(genRoom)) {
      genRoom = Random.nextInt(1000)
    }
    genRoom
  }

  //TODO recheck this method!!
  def convertJavaMutableListToImmutableScala[T](javaList: java.util.List[T]): List[T] = {
    var scalaList = ListBuffer[T]()

    val iter = javaList.iterator()
    while (iter.hasNext) {
      scalaList += iter.next()
    }
    return scalaList.toList

  }


}
