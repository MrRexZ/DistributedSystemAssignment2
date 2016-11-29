package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User
import com.sunway.network.Client
import com.sunway.network.actors.GameplayActorMessages._
import com.sunway.screen.gamescreen.MainGame._

import scala.collection.mutable

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
class Character(val coordVec: Vec, val charID: Int) extends DynaBall(coordVec, radius = 30, mass = 1.0f, false) {
  val uke_stand = image("uke-stand.png", 56, 70, 96, 0, 160, 200)
  // 48, 60
  val uke_animation = animation("uke-animation.png", 56, 70, 160, 200, 6)
  private val max_jump = 10
  private val max_jump2 = 10
  private val max_forward = 10
  private val forward_force = 8000
  private val downward_force = -1000
  private val moves = mutable.HashMap[String, Boolean]("up" -> false, "left" -> false, "down" -> false, "right" -> false)
  private val STOP_X_MOVEMENT = "0"
  private val SEND_COORDINATES = "1"
  private val SEND_VELOCITY = "2"
  private val SEND_FORCE = "3"
  private var frame = 0
  private var num_jump = 0
  private var num_jump2 = 0
  private var num_forward = 0


  init {
    setDefaultSpeed()
    coord = coordVec
    velocity = Vec(ukeSpeed, 0)
  }




  if (isMyChar) registerController()

  def isMyChar = charID == User.myRoomPos.string.toInt

  def registerController() {
    key(KEY_Z, onKeyDown = {
      if (num_jump == 0 && num_jump2 == 0) {
        callEvent(SEND_FORCE, (0f, 4000f))
        num_jump = max_jump
      } else if (num_jump2 == 0) {
        //  callEvent(SEND_VELOCITY, (0.toFloat, -velocity.y.toFloat, false))
        callEvent(SEND_FORCE, (0f, 4000f))
        num_jump2 = max_jump2
      }
    })

    key(KEY_A, onKeyDown = {
      body.setIsResting(false)
      // velocity_=(Vec(-60, velocity.y))
      callEvent(SEND_VELOCITY, (-60.toFloat, velocity.y, body.isResting))



    },
      onKeyUp = {
          callEvent(STOP_X_MOVEMENT)
          callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))
      })

    key(KEY_D, onKeyDown = {
      body.setIsResting(false)
      callEvent(SEND_VELOCITY, (60.toFloat, velocity.y, body.isResting))
      //  callEvent(SEND_COORDINATES)


    }, onKeyUp = {
        callEvent(STOP_X_MOVEMENT)
        callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))

    })


    key(KEY_S, onKeyDown = {
      callEvent(SEND_FORCE, (0.toFloat, downward_force.toFloat))
      callEvent(SEND_VELOCITY, (0.toFloat, velocity.y.toFloat, false))
    })

    key(KEY_X, onKeyDown = {
      if (num_forward == 0) {
        callEvent(SEND_FORCE, (forward_force.toFloat, 0f))
        num_forward = max_forward
      }
    })

    onEvent(STOP_X_MOVEMENT) {

      velocity_=(Vec(0, velocity.y))
      body.setIsResting(true)

    }

    onEvent(SEND_COORDINATES) {
      Client.clientActor ! SendCoordinatesFromMe(body.getPosition.getX, body.getPosition.getY)
    }

    onEventWithArguments(SEND_FORCE) {
      case (forceX: Float, forceY: Float) => {
        addForce(Vec(forceX, forceY))
        Client.clientActor ! SendForceFromMe(forceX, forceY)
      }
    }

    onEventWithArguments(SEND_VELOCITY) {
      case (speedX: Float, speedY: Float, restingState: Boolean) => {
        velocity_=(Vec(speedX, speedY))
        Client.clientActor ! SendVelocity(speedX, speedY, restingState)
      }
    }
  }

  action {
    if (isMyChar) {
      if (body.getVelocity.lengthSquared() != 0) callEvent(SEND_VELOCITY, (body.getVelocity.getX, body.getVelocity.getY, body.isResting))
      else callEvent(SEND_COORDINATES)
    }

  }



  /*action(3000) {
    if(body.isResting==true) callEvent(SEND_COORDINATES)
  }
*/

  actionStaticPeriod(100) {
    frame += 1
    if (frame >= 6) frame = 0
  }

  actionStaticPeriod(200) {
    if (isTouching) {
      if (num_jump > 0) num_jump -= 1
      if (num_jump2 > 0) num_jump2 -= 1
      if (num_forward > 0) num_forward -= 1
    }
  }


  render(-2) {

    if (velocity.x != 0 && isTouching) {
      drawDisplayList(uke_animation(frame), coord)
    } else drawDisplayList(uke_stand, coord)
  }

}
