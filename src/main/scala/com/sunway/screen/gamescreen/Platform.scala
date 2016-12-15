package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.physics.objects.StaticPolygon

/**
  * Created by Mr_RexZ on 11/24/2016.
  */

import com.sunway.screen.gamescreen.MainGame._

class Platform(platform_points: Vec*) extends StaticPolygon(platform_points: _*) {

  init {

    platform_points.foreach(point => tracer.addTrace(point, new Trace {
      def state = State("type" -> "walls")

      def changeState(changer: Trace, s: State) {
      }
    }))
  }

  private val render_id = render {
    if (physics.containsPhysical(this)) {
      //  tracesList
      // tracesList.foreach(trace => drawPolygon(trace.location))
      //  drawTraceGrid(tracer, GRAY)
      drawPolygon(points, /*platform_color*/ BLACK)
    }
    else {
      deleteSelfNoWarn()
    }
  }

  clear {
    delOperationNoWarn(render_id)
    deleteSelfNoWarn()
  }
}