package com.sunway.model

import akka.util.Timeout
import com.github.dunnololda.scage.support.Vec
import com.sunway.screen.gamescreen.Character
import com.sunway.util.MutableString

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object User {

  val WAITING_STATE = 0
  val READY_STATE = 1
  val maxPlayerInRoom = 2
  val HOST_ROOM_ID = 0

  implicit val timeout = Timeout(5.seconds)


  val targetRoomNum: MutableString = new MutableString("0")
  var myName: MutableString = new MutableString(System.getProperty("user.name"))
  var myRoomPos: MutableString = new MutableString("")
  var myPassword: MutableString = new MutableString("")
  var playerNames = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
  var playerRoomStats = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
  var gameState = WAITING_STATE
  var mapState = WAITING_STATE
  var readyPlay = false
  var roomStateSeenUser = new MutableString("WAITING STATE")
  var newPlayerJoining = false
  var newPlayerPos: Option[Int] = None
  var newPlayerVec = Vec()
  var oldPlayerLeave = false
  var oldPlayerPos: Option[Int] = None


  //GAMEPLAY
  var charactersPos: Array[Vec] = Array(Vec(20, ConfigurationObject.windowHeight / 2 - 70), (Vec(150, ConfigurationObject.windowHeight / 2 - 70)))
  var charactersObj = Array.fill[Character](maxPlayerInRoom)(null)
  var mapInformation = List[ArrayBuffer[Tuple2[Float, Float]]]()

}
