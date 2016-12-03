package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.physics.objects.StaticPolygon

/**
  * Created by Mr_RexZ on 11/24/2016.
  */

import com.sunway.screen.gamescreen.MainGame._

class Platform(platform_points: Vec*) extends StaticPolygon(platform_points: _*) {

  init {

    body.setRestitution(0f)
  }
  private val render_id = render {
    if (physics.containsPhysical(this)) drawPolygon(points, /*platform_color*/ BLACK)
    else {
      //println("youo")
      deleteSelfNoWarn()
    }
  }

  clear {
    delOperationNoWarn(render_id)
    deleteSelfNoWarn()
  }
}