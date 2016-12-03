package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.Vec
import com.sunway.model.User._
import com.sunway.screen.gamescreen.MainGame._

/**
  * Created by Mr_RexZ on 11/30/2016.
  */
class Bullet(id: Int, init_coord: Vec, targetCoord: Vec) extends DynaBall(init_coord, radius = 30, mass = 0.1f, false) {
  val ROCKET_ANIMATION = animation("rocket_animation.png", 10, 29, 14, 44, 3)
  val EXPLOSION_ANIMATION = animation("explosion_animation.png", 36, 35, 72, 69, 3)

  private val time = 10f
  val distanceX = (targetCoord.x - init_coord.x).toFloat
  val distanceY = (targetCoord.y - init_coord.y).toFloat
  val speedX = distanceX / time
  val speedY = distanceY / time
  val deltaCoord = Vec(speedX, speedY)

  init {
    coord = init_coord
    velocity_=(deltaCoord)
    val forceDeltaCoord = Vec(deltaCoord.x * 200, deltaCoord.y * 50)
    addForce(forceDeltaCoord)
    this.body.addExcludedBody(charactersObj(id).body)
    body.addExcludedBody(LevelDrawer.flag.body)
  }

  private var next_frame = 0
  val bulletFrame = action {
    next_frame += 1
    if (next_frame >= ROCKET_ANIMATION.length) next_frame = 0
    if (this.isTouching) {
      delOperations(bulletRender)
      deleteSelfNoWarn()
      physics.removePhysicals(this)
    }

    Unit
  }


  val bulletRender = render {
    drawDisplayList(ROCKET_ANIMATION(next_frame), coord)
  }

  clear {
    delOperations(bulletRender, bulletFrame)
    deleteSelfNoWarn()
  }


}
