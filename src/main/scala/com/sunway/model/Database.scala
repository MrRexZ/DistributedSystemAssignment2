package com.sunway.model

import akka.actor.ActorRef
import com.google.common.collect.{BiMap, HashBiMap}

import scala.collection.mutable
import scala.collection.mutable.{ListBuffer, Map}

/**
  * Created by Mr_RexZ on 11/26/2016.
  */
object Database {

  //TODO change ActorRef to ActorSelection
  val roomActorRefPair = mutable.Map[Int, ListBuffer[Option[ActorRef]]]()
  val clientRoomState = Map[Int, ListBuffer[Int]]()
  val roomIsPlaying = Map[Int, Boolean]()


  //TODO Create an authentication system later on to validate username.
  var userNameToPassword = scala.collection.mutable.Map[String, String]()
  var heartBeatActorRef = Map[Int, ActorRef]()
  //TODO Resolve situation when name clashes happen in the BiMap
  var actorReftoUsername: BiMap[ActorRef, String] = HashBiMap.create()


  var roomNumList = Set[Integer]()

}
