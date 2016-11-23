package com.sunway.screen.menu

import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User._
import com.sunway.util.Render._
import com.sunway.util.{ImmutableMessage, Message, MutableMessage}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * Created by Mr_RexZ on 11/17/2016.
  */


//TODO Player reference not assigned yet when player first time joining
object RoomMenu extends ScageScreen("AnotherApp") {
  backgroundColor = BLACK
  val physics = ScagePhysics()

  preinit {
    physics.addPhysicals(
      new MenuEdge(Vec(200, 100), Vec(200, windowHeight - 100)),
      new MenuEdge(Vec(200, windowHeight - 100), Vec(windowWidth - 200, windowHeight - 100)),
      new MenuEdge(Vec(windowWidth - 200, windowHeight - 100), Vec(windowWidth - 200, 100)),
      new MenuEdge(Vec(windowWidth - 200, 100), Vec(200, 100)))
  }
  val boxes = ArrayBuffer[DynaBox](
    SubmitBox,
    CancelBox)
  init {
    stringList.++=(initPlayerName)
    stringList.++=(initPrefixTexts)
  }

  render {
    renderTextFunctions(stringList)
  }
  var stringList = ListBuffer[Message]()
  init {
    selected_box = None
  }
  private var selected_box: Option[DynaBox] = None


  leftMouse(onBtnDown = { m => {
    println(s"loc of x : $m.x")
    selected_box = boxes.find(p => withinX(p) && withinY(p))
    selected_box match {
      case Some(box) => {
        if (box.eq(SubmitBox)) {
          stop()
          println("You pressed submit!")
        }
        else if (box.eq(CancelBox)) {
          stop()
          println("You pressed cancel!")
        }
      }
      case None => selected_box = boxes.find(p => withinX(p) && withinY(p))

    }


    def withinX(p: DynaBox): Boolean = (m.x < p.coord.x + p.box_width - p.box_width / 2) && (m.x > p.coord.x - p.box_width / 2)
    def withinY(p: DynaBox): Boolean = m.y < p.coord.y + p.box_height - p.box_height / 2 && m.y > p.coord.y - p.box_height / 2

  }
    //RoomMenu.stop
  })

  def initPlayerName: ListBuffer[Message] = {

    val playerNamesWithPos = ListBuffer[Message]()
    val playerTextPosition: List[Vec] = List(Vec(220, 320), Vec(220, 300), Vec(220, 280), Vec(220, 260))
    for (pos <- 0 until maxPlayerInRoom) {
      playerNamesWithPos += MutableMessage(playerNames(pos), playerTextPosition(pos))
    }

    return playerNamesWithPos
  }

  def initPrefixTexts: ListBuffer[Message] = {
    val menuTextWithPos = ListBuffer[Message]()
    val menuTextPosition: List[Vec] = List(Vec(270, 320))
    val myRoomPosString = myRoomPos.string
    menuTextWithPos += ImmutableMessage(s"My room : $targetRoomNum at pos : $myRoomPosString", menuTextPosition(0))

    return menuTextWithPos
  }

}

import com.github.dunnololda.scage.ScageLib._
import com.sunway.screen.menu.RoomMenu._

class MenuEdge(from: Vec, to: Vec) extends StaticLine(from, to) {

  render {
    currentColor = WHITE
    drawLine(Vec(points(0).x, points(0).y),
      Vec(points(1).x, points(1).y))
  }
}

object SubmitBox extends DynaBox(Vec(420, 30), 70f, 70f, box_mass = 1000f, restitution = false) {
  render {
    drawPolygon(points, WHITE)
  }
}

object CancelBox extends DynaBox(Vec(50, 30), 70f, 70f, box_mass = 1000f, restitution = false) {
  render {
    drawPolygon(points, WHITE)
  }
}