package com.sunway.network.actors

import akka.actor.ActorRef

import scala.collection.mutable.ListBuffer

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object MenuActorMessages {


  val GET_HEARTBEAT_P1 = 0
  val GET_HEARTBEAT_P2 = 1
  val GET_HEARTBEAT_P3 = 2
  val GET_HEARTBEAT_P4 = 3


  val GET_REF_SERVER = 4
  val GET_REF_CLIENT = 5

  val OK_REJECTED = 0
  val OK_RECEIVED = 1
  val NOT_OK = 2

  trait ServerReply

  case class AcceptPlayerAsHost(roomNum: Int, playerRoomID: Int, actorRefList: List[Option[ActorRef]]) extends ServerReply

  case class AcceptPlayerAsParticipant(roomNum: Int, playerRoomID: Int, actorRefList: List[Option[ActorRef]], roomPlayingState: Boolean) extends ServerReply

  case class RejectPlayer(reason: String) extends ServerReply

  case class NewParticipant(name: String)

  case class AskIfAssignedWithRoom()

  case class SendRoomState(clientRef: ActorRef, roomNum: Int, roomPos: Int, playerRoomState: Int, text: String)

  case class UpdatePlayerTextState(roomPos: Int, text: String)


  case class SendRequestJoin(actorRef: ActorRef, roomNum: Int, userName: String, password: String)

  case class SendRequestCreateRoom(actorRef: ActorRef, userName: String, password: String)

  case class RemovePlayerNameFromScreen(removedPlayer: Int)

  case class AskNumOfParticipants(roomNum: Int, clientRef: ActorRef)

  case class BeAskedUsername()

  case class BeAskedStats(roomPos: Int)

  case class BeAskedRoomState(playerPos: Int)

  case class BeAskedMapState()

  case class BeAskedPlay()

  case class RestartActor()

  case class AllPlayerReceivedMap(roomNum: Int)

  case class BeAskedRoomIsPlaying(roomNum: Int)


  case class StartMessage()

  case class StopMessage()

  case class HeartbeatMessage(playerID: Int)

  case class RemovePlayerFromRoom(roomNum: Int, posRoom: Int, newListActors: ListBuffer[ActorRef])

  case class FrequencyChangeMessage(interval: Int, clientActors: ListBuffer[Option[ActorRef]])

  case class UpdateClientsList(newList: ListBuffer[Option[ActorRef]])

  case class StartGame(membersRoomList: ListBuffer[Option[ActorRef]])

  case class UpdateRoomToMenuStage(roomNum: Int)

}
