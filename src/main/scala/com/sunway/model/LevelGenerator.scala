package com.sunway.model

import akka.actor.ActorRef
import akka.pattern.ask
import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User.timeout
import com.sunway.network.actors.GameplayActorMessages.{SendMapData, SendMapState}
import com.sunway.screen.gamescreen.Platform

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * Created by Mr_RexZ on 11/27/2016.
  */
class LevelGenerator(roomNum: Int, clientsList: ListBuffer[Option[ActorRef]]) {

  val constant_default_char_speed = 30
  val radius = 30
  private val _platformsPoints = ArrayBuffer[ArrayBuffer[Tuple2[Float, Float]]]()

  def platforms = _platformsPoints

  def genLevel(i: Int, limit: Int, start: Vec, current_width: Int, required_width: Int, current_height: Int): Unit = {
    if (i < limit) {
      genHorLevel(0, i, Random.nextInt(4) + 1, start, current_width, required_width, current_height, i)
      val random_height = (Random.nextInt(5) * 10).toInt + 2 * constant_default_char_speed + 60
      genLevel(i + 1, limit, start + Vec(0, current_height + random_height), current_width, required_width, current_height + random_height)
    }

  }

  def genHorLevel(i: Int, verticalLevel: Int, horizontalLimit: Int, start: Vec, current_width: Int, required_width: Int, current_height: Int, heightID: Int) {
    if (i < horizontalLimit) {
      val random_width = (math.random * 90).toInt + 2 * constant_default_char_speed + 600 - (heightID * 150)
      val leftup_coord = if (start.x != 0) start + Vec(Random.nextInt(100) + 80, Random.nextInt(40) + 40)
      else start

      val num_upper_points = 2 + (math.random * (7 + constant_default_char_speed / 20)).toInt
      val platform_inner_points =
        (for (i <- 2 to num_upper_points)
          yield leftup_coord + Vec(i * random_width / num_upper_points, (-100 + math.random * 100).toInt)).toList
      var farthest_coord = platform_inner_points.last
      var platform_points: List[Vec] = (List(leftup_coord, leftup_coord + Vec((random_width) / num_upper_points, 0)))
      if (verticalLevel == 0) {
        farthest_coord = Vec(farthest_coord.x, platform_points(1).y)
        val anotherfarthest_coord = Vec(farthest_coord.x - 60, farthest_coord.y)
        platform_points = (platform_points ::: List(anotherfarthest_coord, farthest_coord) ::: List(leftup_coord + Vec(random_width, -1 * Random.nextInt(120) - 50), leftup_coord + Vec(0, -120))).reverse
      }
      else {
        platform_points = (platform_points :::
          platform_inner_points :::
          List(leftup_coord + Vec(random_width, -1 * Random.nextInt(120) - 50), leftup_coord + Vec(0, -120))).reverse
      }

      addPlatformPoints(platform_points)


      genHorLevel(i + 1, verticalLevel, horizontalLimit, farthest_coord, current_width + random_width + leftup_coord.ix - start.ix, required_width, current_height, heightID)
    }
  }

  private def addPlatformPoints(platformPoints: List[Vec]): Unit = {
    var arrayMap = ArrayBuffer[Tuple2[Float, Float]]()
    for (platformPoint <- platformPoints) {
      arrayMap += Tuple2(platformPoint.x, platformPoint.y)
    }
    _platformsPoints += arrayMap
  }


  //TODO: add random obstacled to upper platforms

  def upperPlatform(points: List[Vec]) = {
    val upper_platform_points =
      ((for (point <- points) yield point + Vec(0, radius * 6)) :::
        (for (point <- points) yield point + Vec(0, radius * 5)).reverse).reverse
    new Platform(upper_platform_points: _*)
  }

  def infiniteUpperPlatform(points: List[Vec]) = {
    val upper_platform_points =
      (List(Vec(points.head.x, points.head.y + ConfigurationObject.windowHeight), Vec(points.last.x, points.last.y + ConfigurationObject.windowHeight)) :::
        (for (point <- points) yield point + Vec(0, radius * 5)).reverse).reverse
    new Platform(upper_platform_points: _*)
  }

  def sendGeneratedMap(): Unit = {
    for (clientRef <- clientsList
         if !clientRef.isEmpty) {
      val futureSendMap: Future[Int] = (clientRef.get ? SendMapData(_platformsPoints.toList)).mapTo[Int]
      futureSendMap onComplete {
        case Success(state) => {
          println("SUCCESS!!")
          clientRef.get ! SendMapState(state)
        }
        case Failure(state) => {
          println("ERROR IN MAP STATE : " + state)
        }
      }
    }
  }

}
