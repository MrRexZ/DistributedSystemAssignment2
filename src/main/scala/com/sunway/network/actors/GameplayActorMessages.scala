package com.sunway.network.actors

import akka.actor.ActorRef
import com.github.dunnololda.scage.support.Vec

import scala.collection.mutable.ListBuffer

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
object GameplayActorMessages {

  case class UpdateClientsListInGame(roomMembers: ListBuffer[Option[ActorRef]], playerPos: Int)

  case class CreateCharacter(playerPos: Int, coordX: Float, coordY: Float)

  case class RemovePlayer(playerPos: Int)

  case class CreateCharacterFromMe(coordX: Float, coordY: Float)

  case class SendCoordinates(coordinates: Vec)

  case class DistributeMessageToAllClients[T](message: T, roomNum: Int)

  case class SendCoordinatesFromMe(posX: Float, posY: Float)

  case class SendCoordinatesToTarget(posX: Float, posY: Float, fromPlayer: Int)

  case class SendVelocity(speedX: Float, speedY: Float, restingState: Boolean)

  case class UpdateVelocity(speedX: Float, speedY: Float, restingState: Boolean, fromPlayer: Int)

  case class SendForceFromMe(forceX: Float, forceY: Float)

  case class UpdateForce(forceX: Float, forceY: Float, fromPlayer: Int)

  case class SendMapData(mapCoordinates: Array[Tuple2[Float, Float]])

  case class SendMapState(state: Int)

}
