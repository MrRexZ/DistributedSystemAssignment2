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
class Character(coordVec: Vec, charID: Int) extends DynaBall(coordVec, radius = 30, mass = 1.0f, false) {
  val uke_stand = image("uke-stand.png", 56, 70, 96, 0, 160, 200)
  // 48, 60
  val uke_animation = animation("uke-animation.png", 56, 70, 160, 200, 6)
  private val max_jump = 10
  private val max_jump2 = 10
  private val max_forward = 10
  private val forward_force = 100
  private val downward_force = -1000
  private val moves = mutable.HashMap[String, Boolean]("up" -> false, "left" -> false, "down" -> false, "right" -> false)
  private val STOP_X_MOVEMENT = "0"
  private val SEND_COORDINATES = "1"
  private val SEND_VELOCITY = "2"
  private var frame = 0
  private var num_jump = 0
  private var num_jump2 = 0
  private var num_forward = 0


  init {
    setDefaultSpeed()
    coord = coordVec
    velocity = Vec(ukeSpeed, 0)
  }

  onEvent(STOP_X_MOVEMENT) {
    velocity_=(Vec(0, velocity.y))
    body.setIsResting(true)
  }

  onEvent(SEND_COORDINATES) {
    Client.clientActor ! SendCoordinatesFromMe(coord.x, coord.y)
  }

  onEventWithArguments(SEND_VELOCITY) {
    case (speedX: Float, speedY: Float, restingState: Boolean) =>
      Client.clientActor ! SendVelocity(speedX, speedY, restingState)
  }


  if (charID == User.myRoomPos.string.toInt) registerController()

  def registerController() {
    key(KEY_Z, onKeyDown = {
      if (num_jump == 0 && num_jump2 == 0) {
        addForce(Vec(1000, 4000))
        num_jump = max_jump
      } else if (num_jump2 == 0) {
        velocity -= Vec(0, velocity.y)
        addForce(Vec(1000, 4000))
        num_jump2 = max_jump2
      }
      // callEvent(SEND_COORDINATES)
    })

    key(KEY_A, onKeyDown = {
      body.setIsResting(false)
      velocity_=(Vec(-60, velocity.y))
      moves("left") = true
      callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))

      //TODO as a backup
      //  callEvent(SEND_COORDINATES)


    },
      onKeyUp = {
        if (moves("left") == true) {
          callEvent(STOP_X_MOVEMENT)
          moves("left") = false
          callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))
        }
      })

    key(KEY_D, onKeyDown = {
      body.setIsResting(false)
      velocity_=(Vec(60, velocity.y))
      moves("right") = true
      callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))
      //  callEvent(SEND_COORDINATES)


    }, onKeyUp = {
      if (moves("right") == true) {
        callEvent(STOP_X_MOVEMENT)
        moves("right") = false
        callEvent(SEND_VELOCITY, (velocity.x, velocity.y, body.isResting))
      }
    })


    key(KEY_S, onKeyDown = {

      addForce(Vec(0, downward_force))
      velocity = Vec(0, velocity.y)
      // callEvent(SEND_COORDINATES)
    })

    key(KEY_X, onKeyDown = {
      if (num_forward == 0) {
        addForce(Vec(forward_force, 0))
        num_forward = max_forward
      }
      //  callEvent(SEND_COORDINATES)
    })
  }


  actionStaticPeriod(100) {
    frame += 1
    if (frame >= 6) frame = 0
  }

  actionStaticPeriod(200) {
    if (num_jump > 0) num_jump -= 1
    if (num_jump2 > 0) num_jump2 -= 1
    if (num_forward > 0) num_forward -= 1
    //if (Math.abs(velocity.x)>0 &&  ) velocity -= Vec(50,0)
  }


  render(-2) {
    if (velocity.x != 0 && isTouching) {
      drawDisplayList(uke_animation(frame), coord)
    } else drawDisplayList(uke_stand, coord)
  }

}
