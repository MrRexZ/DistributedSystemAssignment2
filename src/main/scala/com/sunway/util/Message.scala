package com.sunway.util

import com.github.dunnololda.scage.ScageLib.Vec

/**
  * Created by Mr_RexZ on 11/17/2016.
  */
trait Message {
  def getMessage: String

  def getVectorPos: Vec

}

case class ImmutableMessage(val message: String, val vectorPos: Vec) extends Message {
  override def getMessage: String = message

  override def getVectorPos: Vec = vectorPos
}

class MutableMessage(var message: MutableString, var vectorPos: Vec) extends Message {
  override def getMessage: String = message.string

  def setMessage(newMessage: String) = message.string = newMessage

  override def getVectorPos: Vec = vectorPos
}

object MutableMessage {

  def apply(str: MutableString, vec: Vec): MutableMessage = new MutableMessage(str, vec)
}

class MutableString(var s: String) {
  var string = s
}

object MutableString {
  def apply(s: String): MutableString = new MutableString(s)
}