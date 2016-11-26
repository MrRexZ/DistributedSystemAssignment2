package com.sunway.model

import com.sunway.screen.gamescreen.Character
import com.sunway.util.MutableString

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object User {

  val WAITING_STATE = 0
  val READY_STATE = 1
  val maxPlayerInRoom = 2
  val HOST_ROOM_ID = 0


  //TODO fix this as this is for debugging
  val targetRoomNum: MutableString = new MutableString("0")
  var myName: MutableString = new MutableString(System.getProperty("user.name"))
  var myRoomPos: MutableString = new MutableString("")
  var myPassword: MutableString = new MutableString("")
  var playerNames = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
  var playerRoomStats = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
  var gameState = WAITING_STATE


  //GAMEPLAY
  var charactersPos: Array[Character] = Array(null, null)
}
