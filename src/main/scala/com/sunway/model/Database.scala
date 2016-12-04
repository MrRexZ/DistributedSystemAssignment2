package com.sunway.model

import akka.actor.ActorRef

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}

/**
  * Created by Mr_RexZ on 11/26/2016.
  */
object Database {

  val roomActorRefPair = mutable.Map[Int, ListBuffer[Option[ActorRef]]]()
  val clientRoomState = Map[Int, ListBuffer[Int]]()
  val roomIsPlaying = Map[Int, Boolean]()
  val temporaryMap = Map[Int, List[ArrayBuffer[Tuple2[Float, Float]]]]()

  var userNameToPassword = scala.collection.mutable.Map[String, String]()
  var heartBeatActorRef = Map[Int, ActorRef]()

}
