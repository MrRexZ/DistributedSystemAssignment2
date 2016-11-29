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
import scala.util.{Failure, Success}

/**
  * Created by Mr_RexZ on 11/27/2016.
  */
class LevelGenerator(roomNum: Int, clientsList: ListBuffer[Option[ActorRef]]) {

  val ukeSpeed = 30
  val radius = 30
  private val _platformsPoints = ArrayBuffer[Tuple2[Float, Float]]()

  def platforms = _platformsPoints

  def genLevel(start: Vec, current_width: Int, required_width: Int) {
    if (current_width < required_width) {
      val random_width = (math.random * 700).toInt + 2 * ukeSpeed + 600
      //val random_width=100
      val leftup_coord = if (start.x != 0) start + Vec(0, 0)
      else start

      val num_upper_points = 2 + (math.random * (7 + ukeSpeed / 20)).toInt
      val platform_inner_points =
        (for (i <- 2 to num_upper_points)
          yield leftup_coord + Vec(i * random_width / num_upper_points, (-100 + math.random * 100).toInt)).toList
      val farthest_coord = platform_inner_points.last
      val platform_points: List[Vec] =
        (List(leftup_coord, leftup_coord + Vec((random_width + 3000) / num_upper_points, 0)) :::
          platform_inner_points :::
          List(leftup_coord + Vec(random_width, -ConfigurationObject.windowHeight), leftup_coord + Vec(0, -ConfigurationObject.windowHeight))).reverse

      addPlatformPoints(platform_points)


      //dad genLevel(farthest_coord, current_width + random_width + leftup_coord.ix - start.ix, required_width)
    }
  }

  private def addPlatformPoints(platformPoints: List[Vec]): Unit = {
    for (platformPoint <- platformPoints) {
      _platformsPoints += Tuple2(platformPoint.x, platformPoint.y)
    }
  }

  /*
  def genLevel(start: Vec, current_width : Int, required_width: Int): Vec = {
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

      addPlatformPoints(platform_points)


      genLevel(farthest_coord, current_width + random_width + leftup_coord.ix - start.ix, required_width)
    } else start
  }
*/

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
    for (clientRef <- clientsList) {
      val futureSendMap: Future[Int] = (clientRef.get ? SendMapData(_platformsPoints.toArray)).mapTo[Int]
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
