package com.sunway.network.actors

import akka.actor.{Actor, ActorIdentity, ActorRef}
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.User
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.ActorsUtil._
import com.sunway.network.actors.GameplayActorMessages._
import com.sunway.network.actors.MenuActorMessages._
import com.sunway.screen.gamescreen.MainGame

import scala.collection.mutable.ListBuffer


/**
  * Created by Mr_RexZ on 11/18/2016.
  */


class ClientActor extends Actor {
  var actorServerRef: Option[ActorRef] = None;

  var clientsActorRef: ListBuffer[Option[ActorRef]] = null

  var latestServerReply: String = ""

  def receive = {

    case ActorIdentity(GET_REF_SERVER, Some(ref)) => actorServerRef = Some(ref)

    case AcceptPlayerAsHost(roomNum, playerRoomID, actorRefList) => {
      latestServerReply = myName + ", you're ACCEPTED as HOST, room num is " + roomNum + "and ROOM ID is " + playerRoomID
      println(latestServerReply)
      assignNameToRoom(roomNum, playerRoomID, actorRefList)
    }

    case AcceptPlayerAsParticipant(roomNum, playerRoomID, actorRefList, roomPlayingState) => {
      latestServerReply = myName + ", you're ACCEPTED as PARTICIPANT, room num is " + roomNum + "and ROOM ID is " + playerRoomID
      println(latestServerReply)
      if (roomPlayingState) roomStateSeenUser.string = "PLAYING STATE"
      else roomStateSeenUser.string = "WAITING STATE"
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

    case StartGame(membersRoomList) => {
      gameState = READY_STATE
      clientsActorRef = membersRoomList
    }

    case SendMapData(mapCoordinates) => {
      mapInformation = mapCoordinates
      mapState = READY_STATE
      sender ! targetRoomNum.string.toInt
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

    case _ => println("MESSAGE UNKNOWN IN CLIENT ROOM STATE BEHAVIOUR")
  }

  def assignNameToRoom(roomNum: Int, playerRoomID: Int, actorRefList: List[Option[ActorRef]]): Unit = {
    try {
      targetRoomNum.string = roomNum.toString
      ActorsUtil.updateMembersNameList(actorRefList)
      myRoomPos.string = playerRoomID.toString
    }
    finally {
      Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")
    }
  }

  def bePlayer: Actor.Receive = {

    case InformWon() => {
      MainGame.pause()
      MainGame.won = true
    }

    case InformLost(playerWin) => {
      MainGame.pause()
      MainGame.lost = true
      MainGame.winningPlayer = Some(playerWin)
    }


    case UpdateClientsListNewPlayerInGame(roomMembers, playerPos) => {
      clientsActorRef = roomMembers
      ActorsUtil.updateMembersNameList(roomMembers.toList)
      val newCharPos = charactersPos(playerPos)
      self ! CreateCharacter(playerPos, newCharPos.x, newCharPos.y)
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
      if (charactersObj(fromPlayer) != null) {
        charactersObj(fromPlayer).body.setPosition(posX, posY)
      }
    }
    case UpdateVelocity(speedX, speedY, restingState, fromPlayer) => {
      if (charactersObj(fromPlayer) != null) {
        charactersObj(fromPlayer).velocity_=(Vec(speedX, speedY))
        charactersObj(fromPlayer).body.setIsResting(restingState)
      }
    }

    case UpdateForce(forceX, forceY, fromPlayer) => {
      if (charactersObj(fromPlayer) != null) {
        charactersObj(fromPlayer).body.setForce(forceX, forceY)
      }
    }

    case UpdateDirection(previousXSpeed, fromPlayer) => if (charactersObj(fromPlayer) != null) charactersObj(fromPlayer).previousXSpeed = previousXSpeed

    case CreateBullet(fromPlayer, coordX, coordY, targetCoorX, targetCoorY) => {
      if (charactersObj(fromPlayer) != null) charactersObj(fromPlayer).assignBullet(fromPlayer, Vec(coordX, coordY), Vec(targetCoorX, targetCoorY))
    }

    case UpdateClientsListRemovePlayerInGame(clientActors, removedPlayer) => {

      self ! RemovePlayer(removedPlayer)
      ActorsUtil.updateMembersNameList(clientActors.toList)
    }


    case SendCoordinatesFromMe(posX, posY) => sendMessagesToAllClientsNotMe(SendCoordinatesToTarget(posX, posY, myRoomPos.string.toInt), clientsActorRef.toList, self)
    case SendVelocity(speedX, speedY, restingState) => sendMessagesToAllClientsNotMe(UpdateVelocity(speedX, speedY, restingState, myRoomPos.string.toInt), clientsActorRef.toList, self)
    case SendForceFromMe(forceX, forceY) => sendMessagesToAllClientsNotMe(UpdateForce(forceX, forceY, myRoomPos.string.toInt), clientsActorRef.toList, self)
    case CreateCharacterFromMe(coordX, coordY) => sendMessagesToAllClientsNotMe(CreateCharacter(myRoomPos.string.toInt, coordX, coordY), clientsActorRef.toList, self)
    case UpdateDirectionFromMe(previousXSpeed) => sendMessagesToAllClientsNotMe(UpdateDirection(previousXSpeed, myRoomPos.string.toInt), clientsActorRef.toList, self)
    case CreateBulletFromMe(coordX, coordY, targetCoorX, targetCoorY) => sendMessagesToAllClientsNotMe(CreateBullet(myRoomPos.string.toInt, coordX, coordY, targetCoorX, targetCoorY), clientsActorRef.toList, self)
    case InformWinState() => sendMessagesToAllClientsNotMe(InformLost(myRoomPos.string.toInt), clientsActorRef.toList, self)

    case ChangeMenuState(matchState) => {
      gameState = WAITING_STATE
      mapState = WAITING_STATE
      readyPlay = false
      roomStateSeenUser.string = "WAITING STATE"
      context.become(receive)

      Client.actorServerSelect ! SendRoomState(self, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")

      if (matchState == MainGame.MATCH_END || ifNoPeopleLeft) {
        Client.actorServerSelect ! UpdateRoomToMenuStage(targetRoomNum.string.toInt)
        sendMessagesToAllClientsNotMe(ChangeMenuState(matchState), clientsActorRef.toList, self)
      }
    }

      def ifNoPeopleLeft: Boolean = {
        for (client <- clientsActorRef) {
          if (!client.isEmpty && !client.get.equals(self)) return false
        }
        true
      }

    case UpdatePlayerTextState(roomPos, playerRoomState) => {
      playerRoomStats(roomPos).string = playerRoomState
    }

    case BeAskedUsername => sender ! myName.string

    case BeAskedStats(roomPos) => {
      sender ! playerRoomStats(roomPos).string
    }

    case BeAskedMapState => {
      sender ! mapState
    }

    case AskMyCharObject(clientRef) => {
      do {
        if (charactersObj(myRoomPos.string.toInt) != null) clientRef ! CreateCharacter(myRoomPos.string.toInt, charactersObj(myRoomPos.string.toInt).coord.x, charactersObj(myRoomPos.string.toInt).coord.y)
      } while (charactersObj(myRoomPos.string.toInt) == null)
    }

    case SendMyCharacterObject(playerPos, x, y) => {
      sendMessagesToAllClientsNotMe(AskMyCharObject(self), clientsActorRef.toList, self)
    }

    case RemoveMyCharacterObject() => {
      sendMessagesToAllClientsNotMe(RemovePlayer(myRoomPos.string.toInt), clientsActorRef.toList, self)
    }

    case UpdateClientsList(roomMembers) => {
      clientsActorRef = roomMembers
    }
    case _ => println("MESSAGE NOT DETECTED IN GAME")
  }

}
