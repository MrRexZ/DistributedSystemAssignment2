package com.sunway.network.actors

import com.github.dunnololda.scage.support.Vec

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
object GameplayActorMessages {

  case class SendCoordinates(coordinates: Vec)

  case class DistributeMessageToAllClients[T](message: T, roomNum: Int)

  case class SendCoordinatesFromMe(posX: Float, posY: Float)

  case class SendCoordinatesToTarget(posX: Float, posY: Float, fromPlayer: Int)

  case class SendVelocity(speedX: Float, speedY: Float, restingState: Boolean)

  case class UpdateVelocity(speedX: Float, speedY: Float, restingState: Boolean, fromPlayer: Int)

}
