package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.User._
import com.sunway.screen.gamescreen.MainGame._

/**
  * Created by Mr_RexZ on 11/30/2016.
  */
class Bullet(id: Int, init_coord: Vec, targetCoord: Vec) extends DynaBall(init_coord, radius = 30, mass = 0.1f, false) with MoveableObject {
  val ROCKET_ANIMATION = animation("rocket_animation.png", 10, 29, 14, 44, 3)
  val EXPLOSION_ANIMATION = animation("explosion_animation.png", 36, 35, 72, 69, 3)

  private val time = 10f
  val distanceX = (targetCoord.x - windowWidth / 2).toFloat
  val distanceY = (targetCoord.y - windowHeight / 2).toFloat
  val speedX = distanceX / time
  val speedY = distanceY / time
  val deltaCoord = Vec(speedX, speedY)
  var crash = false
  var timer = 0

  rotation = -Math.toDegrees(Math.atan2(targetCoord.x - windowWidth / 2, targetCoord.y - windowHeight / 2)).toFloat;
  if (rotation < 0) rotation += 360


  val bulletInit = init {
    coord = init_coord
    velocity_=(step)
    addForce(step * 1000)
    // val forceDeltaCoord = Vec(deltaCoord.x * 200, deltaCoord.y * 50)
    // addForce(forceDeltaCoord)
    body.addExcludedBody(charactersObj(id).body)
    body.addExcludedBody(LevelDrawer.flag.body)
  }

  private var next_frame = 0
  val bulletFrame = action {
    println(coord)
    next_frame += 1
    if (next_frame >= ROCKET_ANIMATION.length) next_frame = 0


    Unit
  }

  val bulletCheckTouch = action {
    if (this.isTouching) {
      crash = true
      deleteSelfNoWarn()
      physics.removePhysicals(this)
    }
  }



  val bulletRender = render {

    if (crash == false) {
      openglMove(coord)
      openglRotateDeg(rotation)
      drawDisplayList(ROCKET_ANIMATION(next_frame), Vec.zero)
    }
    else {
      openglMove(coord)
      drawDisplayList(EXPLOSION_ANIMATION.head, Vec.zero)
      timer += 1
      if (timer == 1000) {
        delOperations(bulletInit, bulletCheckTouch)
        deleteSelf()
      }
    }
  }

  clear {
    delOperations(bulletRender, bulletFrame, bulletInit, bulletCheckTouch)
    deleteSelfNoWarn()
  }


}