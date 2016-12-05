package com.sunway.model

import akka.actor.ActorRef
import akka.pattern.ask
import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User.timeout
import com.sunway.network.Server
import com.sunway.network.actors.GameplayActorMessages.SendMapData
import com.sunway.network.actors.MenuActorMessages.AllPlayerReceivedMap

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

/**
  * Created by Mr_RexZ on 11/27/2016.
  */
class LevelGenerator(roomNum: Int, clientsList: ListBuffer[Option[ActorRef]]) {

  private val constant = 30
  private val radius = 30
  private val platformsPoints = ArrayBuffer[ArrayBuffer[Tuple2[Float, Float]]]()

  def platforms = platformsPoints

  def genLevel(i: Int, limit: Int, start: Vec, current_width: Int, required_width: Int, current_height: Int): Unit = {
    if (i <= limit) {
      genHorLevel(0, i, Random.nextInt(4) + 1, start, current_width, required_width, current_height, i, limit)
      val random_height = (Random.nextInt(5) * 10).toInt + 2 * constant + 60
      genLevel(i + 1, limit, start + Vec(0, current_height + random_height), current_width, required_width, current_height + random_height)
    }

  }

  private def genHorLevel(i: Int, verticalLevel: Int, horizontalLimit: Int, start: Vec, current_width: Int, required_width: Int, current_height: Int, heightID: Int, verticalLimit: Int) {
    if (i <= horizontalLimit) {
      val random_width = (math.random * 90).toInt + 2 * constant + 600 - (heightID * 150)
      val leftup_coord = if (start.x != 0) start + Vec(Random.nextInt(100) + 80, Random.nextInt(40) + 40)
      else start

      val num_upper_points = 2 + (math.random * (7 + constant / 20)).toInt
      val platform_inner_points =
        (for (i <- 2 to num_upper_points)
          yield leftup_coord + Vec(i * random_width / num_upper_points, (-100 + math.random * 100).toInt)).toList
      var farthest_coord = platform_inner_points.last
      var platform_points: List[Vec] = (List(leftup_coord, leftup_coord + Vec((random_width) / num_upper_points, 0)))
      if (verticalLevel == verticalLimit && i == horizontalLimit) {
        platform_points = (platform_points ::: List(farthest_coord) ::: List(leftup_coord + Vec(random_width, -1 * Random.nextInt(120) - 50), leftup_coord + Vec(0, -120))).reverse
      }
      else {
        platform_points = (platform_points :::
          platform_inner_points :::
          List(leftup_coord + Vec(random_width, -1 * Random.nextInt(120) - 50), leftup_coord + Vec(0, -120))).reverse
      }

      addPlatformPoints(platform_points)
      genHorLevel(i + 1, verticalLevel, horizontalLimit, farthest_coord, current_width + random_width + leftup_coord.ix - start.ix, required_width, current_height, heightID, verticalLimit)
    }
  }

  private def addPlatformPoints(platformPoints: List[Vec]): Unit = {
    var arrayMap = ArrayBuffer[Tuple2[Float, Float]]()
    for (platformPoint <- platformPoints) {
      arrayMap += Tuple2(platformPoint.x, platformPoint.y)
    }
    platformsPoints += arrayMap
  }

  def sendGeneratedMap(): Unit = {
    for (clientRef <- clientsList
         if !clientRef.isEmpty) {
      val futureSendMap: Future[Int] = (clientRef.get ? SendMapData(platformsPoints.toList)).mapTo[Int]
      futureSendMap onComplete {
        case Success(roomNum) => {

          Server.serverActor ! AllPlayerReceivedMap(roomNum)
        }
        case Failure(state) => {
          println("ERROR IN MAP STATE : " + state)
        }
      }
    }
  }

}
