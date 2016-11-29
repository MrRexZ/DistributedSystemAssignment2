package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.support.Vec
import com.github.dunnololda.scage.support.physics.Physical
import com.sunway.model.User._
import com.sunway.screen.gamescreen.MainGame._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by Mr_RexZ on 11/27/2016.
  */
object LevelDrawer {

  private val platformArray = ArrayBuffer[Physical]()


  /*
  if (platform_inner_points.length > 4 && math.random > 0.5) {
    val upper_platform = (math.random * 2).toInt match {
      case 0 => infiniteUpperPlatform(platform_inner_points.init)
      case _ => upperPlatform(platform_inner_points.init.tail)
    }
    addPlatform(upper_platform)
  }
  */


  def generatePlatformsInUser(): Unit = {
    //TODO check if the method below is executed

    val vecLevel = convertToVec(mapInformation)
    val platform = new Platform(vecLevel: _*)
    addPlatform(platform)
  }

  def convertToVec(mapInformation: Array[Tuple2[Float, Float]]): List[Vec] = {
    var tempVec = ListBuffer[Vec]()
    for (platPoint <- mapInformation) {
      tempVec += Vec(platPoint._1, platPoint._2)
    }
    tempVec.toList
  }

  def addPlatform(platform: Physical): Unit = {
    physics.addPhysical(platform)
    platformArray += platform
  }
}
