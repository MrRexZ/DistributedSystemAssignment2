package com.sunway.model

import com.sunway.util.MutableString

/**
  * Created by Mr_RexZ on 11/18/2016.
  */
object User {

  val maxPlayerInRoom = 2
  val HOST_ROOM_ID = 0
  //TODO fix this too
  val targetRoomNum: MutableString = new MutableString("0")
  var myName: MutableString = new MutableString(System.getProperty("user.name"))
  var myRoomPos: MutableString = new MutableString("")
  var myPassword: MutableString = new MutableString("")
  var playerNames = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
}
