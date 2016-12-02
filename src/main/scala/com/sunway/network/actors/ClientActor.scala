package com.sunway.network.actors

import akka.actor.{Actor, ActorIdentity, ActorRef}
import akka.util.Timeout
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.User
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.GameplayActorMessages._
import com.sunway.network.actors.MenuActorMessages._
import com.sunway.screen.gamescreen.MainGame

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration._


/**
  * Created by Mr_RexZ on 11/18/2016.
  */


class ClientActor extends Actor {
  var actorServerRef: Option[ActorRef] = None;

  //TODO work on remotely getting the data
  var clientsActorRef: ListBuffer[Option[ActorRef]] = null

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
      ActorsUtil.updateMembersNameList(roomMembers.toList)
    }

    case UpdatePlayerTextState(roomPos, playerRoomState) => {
      playerRoomStats(roomPos).string = playerRoomState
    }

    case BeAskedStats(roomPos) => {
      sender ! playerRoomStats(roomPos).string
    }

    //TODO rework this when onQuit
    case StartGame(membersRoomList) => {
      gameState = READY_STATE
      clientsActorRef = membersRoomList
      println("MORE THE STATE : " + clientsActorRef)
    }

    case SendMapData(mapCoordinates) => {
      mapInformation = mapCoordinates
      sender ! READY_STATE
    }

    case SendMapState(state) => {
      mapState = state
      Client.actorServerSelect ! AllPlayerReceivedMap(targetRoomNum.string.toInt)
    }

    case BeAskedRoomState(playerPos) => {
      sender ! gameState
    }

    case BeAskedMapState => {
      sender ! mapState
    }

    case BeAskedPlay() => {
      readyPlay = true
      context.become(bePlayer)
      println(s"player ${User.myRoomPos.string.toInt} IS ASKED TO PLAY")
    }

    //   case RestartActor => throw new IllegalStateException()

    case _ => println("MESSAGE UNKNOWN")
  }

  def assignNameToRoom(roomNum: Int, playerRoomID: Int, actorRefList: List[Option[ActorRef]]): Unit = {
    try {
      inRoom = true
      targetRoomNum.string = roomNum.toString
      ActorsUtil.updateMembersNameList(actorRefList)
      myRoomPos.string = playerRoomID.toString
    }
    finally {
      Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")
    }
  }

  //TODO work on this receive method. It needs constant update of the list from heartbeat!!
  def bePlayer: Actor.Receive = {

    case InformWon() => {
      println("EXECUTED INFORM WON ")
      MainGame.pause()
      MainGame.won = true
    }

    case InformLost(playerWin) => {
      MainGame.pause()
      MainGame.lost = true
      MainGame.winningPlayer = Some(playerWin)
    }


    case UpdateClientsListInGame(roomMembers, playerPos) => {
      clientsActorRef = roomMembers
      ActorsUtil.updateMembersNameList(roomMembers.toList)
      self ! CreateCharacter(playerPos, 50, 100)
    }

    case CreateCharacter(playerPos, coordX, coordY) => {
      newPlayerJoining = true
      newPlayerPos = Option(playerPos)
      newPlayerVec = Vec(coordX, coordY)
    }

    case RemovePlayer(playerPos) => {
      oldPlayerLeave = true
      oldPlayerPos = Option(playerPos)
    }

    case SendCoordinatesToTarget(posX, posY, fromPlayer) => {
      if (charactersObj(fromPlayer) != null) charactersObj(fromPlayer).body.setPosition(posX, posY)
    }
    case UpdateVelocity(speedX, speedY, restingState, fromPlayer) => {
      var charToUpdate = charactersObj(fromPlayer)
      charToUpdate.velocity_=(Vec(speedX, speedY))
      charToUpdate.body.setIsResting(restingState)
    }

    case UpdateForce(forceX, forceY, fromPlayer) => {
      charactersObj(fromPlayer).body.setForce(forceX, forceY)
    }

    case UpdateDirection(previousXSpeed, fromPlayer) => charactersObj(fromPlayer).previousXSpeed = previousXSpeed

    case CreateBullet(fromPlayer, coordX, coordY, targetCoorX, targetCoorY) => {
      charactersObj(fromPlayer).assignBullet(fromPlayer, Vec(coordX, coordY), Vec(targetCoorX, targetCoorY))
    }

    case UpdateClientsList(roomMembers) => {
      println("EXECUTED CLIENTS LIST")
      self ! InformWon()
      ActorsUtil.updateMembersNameList(roomMembers.toList)
    }



    case SendCoordinatesFromMe(posX, posY) => sendMessagesToAllClientsNotMe(SendCoordinatesToTarget(posX, posY, myRoomPos.string.toInt), clientsActorRef.toList)
    case SendVelocity(speedX, speedY, restingState) => sendMessagesToAllClientsNotMe(UpdateVelocity(speedX, speedY, restingState, myRoomPos.string.toInt), clientsActorRef.toList)
    case SendForceFromMe(forceX, forceY) => sendMessagesToAllClientsNotMe(UpdateForce(forceX, forceY, myRoomPos.string.toInt), clientsActorRef.toList)
    case CreateCharacterFromMe(coordX, coordY) => sendMessagesToAllClientsNotMe(CreateCharacter(myRoomPos.string.toInt, coordX, coordY), clientsActorRef.toList)
    case UpdateDirectionFromMe(previousXSpeed) => sendMessagesToAllClientsNotMe(UpdateDirection(previousXSpeed, myRoomPos.string.toInt), clientsActorRef.toList)
    case CreateBulletFromMe(coordX, coordY, targetCoorX, targetCoorY) => sendMessagesToAllClientsNotMe(CreateBullet(myRoomPos.string.toInt, coordX, coordY, targetCoorX, targetCoorY), clientsActorRef.toList)
    case InformWinState() => sendMessagesToAllClientsNotMe(InformLost(myRoomPos.string.toInt), clientsActorRef.toList)
    case ChangeMenuState() => {
      gameState = WAITING_STATE
      mapState = WAITING_STATE
      readyPlay = false
      context.become(receive)
      Client.actorServerSelect ! SendRoomState(self, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")
      sendMessagesToAllClientsNotMe(ChangeMenuState(), clientsActorRef.toList)
    }

    case UpdatePlayerTextState(roomPos, playerRoomState) => {
      playerRoomStats(roomPos).string = playerRoomState
    }

    case _ => println("MESSAGE NOT DETECTED IN GAME")
  }

  def sendMessagesToAllClientsNotMe[T](message: T, listPlayer: List[Option[ActorRef]]): Unit = {
    implicit val timeout = Timeout(5 seconds)
    for (player <- listPlayer
         if !player.isEmpty && !player.get.equals(Client.clientActor))
      player.get ! message
  }


}
