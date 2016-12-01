package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.support.Vec
import com.github.dunnololda.scage.support.physics.Physical
import com.sunway.model.User._
import com.sunway.screen.gamescreen.MainGame._

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Random

/**
  * Created by Mr_RexZ on 11/27/2016.
  */
object LevelDrawer {

  private val platformArray = ArrayBuffer[Physical]()
  val flag = new Flag(Vec(-100, -100), 45, 70)


  /*
  if (platform_inner_points.length > 4 && math.random > 0.5) {
    val upper_platform = (math.random * 2).toInt match {
      case 0 => infiniteUpperPlatform(platform_inner_points.init)
      case _ => upperPlatform(platform_inner_points.init.tail)
    }
    addPlatform(upper_platform)
  }
  */


  def clearPlatform() {
    platformArray.clear()
  }

  def generatePlatformsInUser(): Unit = {
    //TODO check if the method below is executed

    val vecLevel = convertToVec(mapInformation)

  }

  def convertToVec(mapInformation: List[ArrayBuffer[Tuple2[Float, Float]]]) {
    for (poly <- mapInformation) {

      var tempVec = ListBuffer[Vec]()
      for (platPoint <- poly) {
        tempVec += Vec(platPoint._1, platPoint._2)
      }
      val platform = new Platform(tempVec.toList: _*)
      addPlatform(platform)

    }

    val randomIsland = Random.nextInt(mapInformation.last.size)
    for ((lastPoly, index) <- mapInformation.last.zipWithIndex) {
      if (index == 4) {
        createFlag(lastPoly._1, lastPoly._2)
      }
    }

  }

  def addPlatform(platform: Physical): Unit = {
    physics.addPhysical(platform)
    platformArray += platform
  }

  def createFlag(x: Float, y: Float): Unit = {
    flag.coord_=(Vec(x, y))

    MainGame.physics.addPhysical(flag)

  }
}
