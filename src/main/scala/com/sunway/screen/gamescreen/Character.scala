package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.User
import com.sunway.network.Client
import com.sunway.network.actors.GameplayActorMessages._
import com.sunway.screen.gamescreen.MainGame._
import com.sunway.screen.menu.RoomMenu.{action => _, actionStaticPeriod => _, init => _, key => _, leftMouse => _, render => _}

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
class Character(val coordVec: Vec, val charID: Int) extends DynaBall(coordVec, radius = 30, mass = 1.0f, false) {
  val char_stand_right = image("uke-stand.png", 45, 70, 121, 8, 125, 195)
  val char_stand_left = image("stay2.png", 45, 70, 11, 11, 128, 191)
  val char_animation_left = animation("uke.png", 56, 70, 160, 200, 6)
  val char_animation_right = animation("uke-animation.png", 56, 70, 160, 200, 6)

  private val max_jump = 10
  private val max_jump2 = 10
  private val max_forward = 10
  private val forward_force = 8000
  private val downward_force = -1000
  private val STOP_X_MOVEMENT = "0"
  private val SEND_COORDINATES = "1"
  private val SEND_VELOCITY = "2"
  private val SEND_FORCE = "3"
  private val FACE_LEFT = "4"
  private val FACE_RIGHT = "5"
  private val FIRE_WEAPON = "6"
  private var frame = 0
  private var num_jump = 0
  private var num_jump2 = 0
  private var num_forward = 0
  var previousXSpeed = 0
  var bulletSpeed = 5f

  var receiveBulletInPos = Vec()
  var receiveBulletTargetPos = Vec()
  var receiveBulletFrom: Int = 0
  var aBulletReceived = false


  val initChar = init {

    coord_=(coordVec)
    velocity_=(Vec(0, 0))
    physics.addPhysical(this)
    if (isMyChar) registerController()

  }



  def isMyChar = charID == User.myRoomPos.string.toInt

  def registerController() {
    key(KEY_Z, onKeyDown = {
      if (num_jump == 0 && num_jump2 == 0) {
        callEvent(SEND_FORCE, (0f, 13000f))
        num_jump = max_jump
      } else if (num_jump2 == 0) {
        callEvent(SEND_FORCE, (0f, 10000f))
        num_jump2 = max_jump2
      }

    })



    key(KEY_A, onKeyDown = {
      body.setIsResting(false)
      callEvent(SEND_VELOCITY, (-60.toFloat, velocity.y, body.isResting))
    },
      onKeyUp = {
        body.setIsResting(true)
        callEvent(FACE_LEFT)
        callEvent(SEND_VELOCITY, (0f, velocity.y, body.isResting))
      })

    key(KEY_D, onKeyDown = {
      body.setIsResting(false)
      callEvent(SEND_VELOCITY, (60.toFloat, velocity.y, body.isResting))
    }, onKeyUp = {
      body.setIsResting(true)
      callEvent(FACE_RIGHT)
      callEvent(SEND_VELOCITY, (0f, velocity.y, body.isResting))
    })


    key(KEY_S, onKeyDown = {
      callEvent(SEND_FORCE, (0.toFloat, downward_force.toFloat))
      callEvent(SEND_VELOCITY, (0.toFloat, velocity.y.toFloat, false))
    })

    key(KEY_X, onKeyDown = {
      if (num_forward == 0) {
        callEvent(SEND_FORCE, (previousXSpeed * forward_force.toFloat, 0f))
        num_forward = max_forward
      }
    })

    leftMouse(onBtnDown = {
      targetCoor => {
        callEvent(FIRE_WEAPON, targetCoor)

      }
    })

    val sendCoorEvents = onEvent(SEND_COORDINATES) {
      Client.clientActor ! SendCoordinatesFromMe(body.getPosition.getX, body.getPosition.getY)
    }

    val sendForceEvent = onEventWithArguments(SEND_FORCE) {
      case (forceX: Float, forceY: Float) => {
        body.setIsResting(false)
        addForce(Vec(forceX, forceY))
        Client.clientActor ! SendForceFromMe(forceX, forceY)
      }

    }



    val sendVelocityEvent = onEventWithArguments(SEND_VELOCITY) {
      case (speedX: Float, speedY: Float, restingState: Boolean) => {
        velocity_=(Vec(speedX, speedY))
        body.setIsResting(restingState)
        Client.clientActor ! SendVelocity(speedX, speedY, restingState)
      }
    }

    val faceLeftEvent = onEvent(FACE_LEFT) {
      previousXSpeed = -1
      Client.clientActor ! UpdateDirectionFromMe(previousXSpeed)
    }
    val faceRightEvent = onEvent(FACE_RIGHT) {
      previousXSpeed = 1
      Client.clientActor ! UpdateDirectionFromMe(previousXSpeed)
    }

    val eventFireWeapon = onEventWithArguments(FIRE_WEAPON) {
      case (targetCoor: Vec) => {
        val bullet = new Bullet(User.myRoomPos.string.toInt, coord, targetCoor)
        MainGame.physics.addPhysical(bullet)
        Client.clientActor ! CreateBulletFromMe(coord.x, coord.y, targetCoor.x, targetCoor.y)
      }
    }

    clear {
      delEvents(eventFireWeapon, faceLeftEvent, faceRightEvent, sendVelocityEvent, sendForceEvent, sendCoorEvents)
    }
  }

  def assignBullet(fromPlayer: Int, coord: Vec, targetCoor: Vec): Unit = {
    receiveBulletInPos = coord
    receiveBulletTargetPos = targetCoor
    receiveBulletFrom = fromPlayer
    aBulletReceived = true
  }

  val bulletReceive = action {
    if (aBulletReceived == true) {
      val bullet = new Bullet(receiveBulletFrom, receiveBulletInPos, receiveBulletTargetPos)
      MainGame.physics.addPhysical(bullet)
      aBulletReceived = false
    }
  }


  val charTransAction = action {
    if (isMyChar) {
      if (body.getVelocity.lengthSquared() != 0) callEvent(SEND_VELOCITY, (body.getVelocity.getX, body.getVelocity.getY, body.isResting))
      else if (body.getVelocity.lengthSquared() == 0) callEvent(SEND_COORDINATES)
    }
  }


  val frameAction = actionStaticPeriod(100) {
    frame += 1
    if (frame >= 6) frame = 0
  }

  val jumpAction = actionStaticPeriod(100) {
    if (isTouching) {
      if (num_jump > 0) num_jump -= 1
      if (num_jump2 > 0) num_jump2 -= 1
      if (num_forward > 0) num_forward -= 1
    }
  }

  val charRender = render(-2) {
    if (velocity.x > 0 && isTouching) {
      drawDisplayList(char_animation_right(frame), coord)
    }
    else if (velocity.x < 0 && isTouching) {
      drawDisplayList(char_animation_left(frame), coord)
    }
    else {
      if (previousXSpeed < 0) drawDisplayList(char_stand_left, coord)
      else if (previousXSpeed >= 0) drawDisplayList(char_stand_right, coord)
    }

    if (playerIsLeaving) {
      delInit(initChar)
      delActions(charTransAction, frameAction, jumpAction, bulletReceive)
      deleteSelfNoWarn()
    }
  }

  def playerIsLeaving: Boolean = (User.oldPlayerLeave == true && !User.oldPlayerPos.isEmpty && User.oldPlayerPos.get == charID)


  clear {
    physics.removePhysicals(this)
    delInit(initChar)
    delRender(charRender)
    delActions(charTransAction, frameAction, jumpAction, bulletReceive)

  }

}
