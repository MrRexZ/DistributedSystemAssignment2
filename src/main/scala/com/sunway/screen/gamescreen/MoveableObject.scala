package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.support.Vec

/**
  * Created by Mr_RexZ on 12/6/2016.
  */
trait MoveableObject {
  protected var speed = 5.0f
  protected var rotation = 0.0f

  def step = Vec(-0.4f * speed * math.sin(math.toRadians(rotation)).toFloat,
    0.4f * speed * math.cos(math.toRadians(rotation)).toFloat)
}