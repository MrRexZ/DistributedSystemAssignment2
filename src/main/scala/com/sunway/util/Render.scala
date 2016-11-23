package com.sunway.util

import com.github.dunnololda.scage.ScageLib._

import scala.collection.mutable.ListBuffer

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object Render {
  def renderTextFunctions(stringList: ListBuffer[Message]) {

    stringList foreach (obj => print(obj.getMessage, obj.getVectorPos, GREEN))

  }
}
