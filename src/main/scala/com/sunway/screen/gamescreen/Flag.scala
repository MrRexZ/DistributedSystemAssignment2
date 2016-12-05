package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.ScageLibD.DynaBox
import com.github.dunnololda.scage.support.Vec
import com.sunway.screen.gamescreen.MainGame._

/**
  * Created by Mr_RexZ on 12/1/2016.
  */
class Flag(val coordVec: Vec, box_width: Float, box_height: Float) extends DynaBox(coordVec, box_width = box_width, box_height = box_height, box_mass = 0.1f, false) {
  val flag = image("flags.png", box_width, box_height, 1, 20, 9, 12)


  val flagAction = action {
    body.setPosition(coord.x, coord.y)
  }


  val flagRender = render {
    drawDisplayList(flag, coord)
  }


}
