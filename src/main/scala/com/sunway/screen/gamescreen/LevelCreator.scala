package com.sunway.screen.gamescreen

import com.github.dunnololda.scage.ScageLib._

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Mr_RexZ on 11/24/2016.
  */

import com.sunway.screen.gamescreen.MainGame._

object LevelCreator {
  //private val log = MySimpleLogger(this.getClass.getName)

  private val _platforms = ArrayBuffer[Physical]()

  def platforms = _platforms

  def addPlatform(platform: Physical) {
    physics.addPhysical(platform)
    _platforms += platform
  }

  def continueLevel(start: Vec, current_width: Int, required_width: Int): Vec = {
    if (current_width < required_width) {
      val random_width = (math.random * 700).toInt + 2 * ukeSpeed + 600
      val leftup_coord = if (start.x != 0) start + Vec(0, 0)
      else start

      val num_upper_points = 2 + (math.random * (7 + ukeSpeed / 20)).toInt
      val platform_inner_points =
        (for (i <- 2 to num_upper_points)
          yield leftup_coord + Vec(i * random_width / num_upper_points, (-100 + math.random * 100).toInt)).toList
      val farthest_coord = platform_inner_points.last
      val platform_points: List[Vec] =
        (List(leftup_coord, leftup_coord + Vec((random_width + 30000) / num_upper_points, 0)) :::
          platform_inner_points :::
          List(leftup_coord + Vec(random_width, -windowHeight), leftup_coord + Vec(0, -windowHeight))).reverse
      val platform = new Platform(platform_points: _*)
      addPlatform(platform)

      if (platform_inner_points.length > 4 && math.random > 0.5) {
        val upper_platform = (math.random * 2).toInt match {
          case 0 => infiniteUpperPlatform(platform_inner_points.init)
          case _ => upperPlatform(platform_inner_points.init.tail)
        }
        addPlatform(upper_platform)
      }

      continueLevel(farthest_coord, current_width + random_width + leftup_coord.ix - start.ix, required_width)
    } else start
  }


  //TODO: add random obstacled to upper platforms

  def upperPlatform(points: List[Vec]) = {
    val upper_platform_points =
      ((for (point <- points) yield point + Vec(0, myChar.radius * 6)) :::
        (for (point <- points) yield point + Vec(0, myChar.radius * 5)).reverse).reverse
    new Platform(upper_platform_points: _*)
  }

  def infiniteUpperPlatform(points: List[Vec]) = {
    val upper_platform_points =
      (List(Vec(points.head.x, points.head.y + windowHeight), Vec(points.last.x, points.last.y + windowHeight)) :::
        (for (point <- points) yield point + Vec(0, myChar.radius * 5)).reverse).reverse
    new Platform(upper_platform_points: _*)
  }
}