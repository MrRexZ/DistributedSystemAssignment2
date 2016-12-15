package com.sunway.screen.menu

import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.MenuActorMessages.SendRoomState
import com.sunway.screen.gamescreen.MainGame
import com.sunway.util.Render._
import com.sunway.util.{ImmutableMessage, Message, MutableMessage, MutableString}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * Created by Mr_RexZ on 11/17/2016.
  */

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
  private val boxes = ArrayBuffer[DynaBox](
    ReadyBox,
    WaitingBox,
    CancelBox)

  init {

    stringList.++=(initMutableTexts)
    stringList.++=(initPrefixTexts)

    val renderText = render {
      renderTextFunctions(stringList)
    }

    clear {
      delRender(renderText)
    }

    Unit
  }



  var stringList = ListBuffer[Message]()
  init {
    selected_box = None
  }
  private var selected_box: Option[DynaBox] = None


  leftMouse(onBtnDown = { m => {
    selected_box = boxes.find(p => withinX(p) && withinY(p))
    selected_box match {
      case Some(box) => {
        if (box.eq(ReadyBox)) {
          Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.READY_STATE, " - READY")
        }
        else if (box.eq(WaitingBox)) {
          Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")
        }
        else if (box.eq(CancelBox)) {
          Client.clientSystem.stop(Client.clientActor)
          Client.createClientActor

          resetStats
          stop()

          def resetStats: Unit = {
            stringList.clear()
            playerNames = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
            playerRoomStats = Array.fill[MutableString](maxPlayerInRoom)(new MutableString(""))
            myRoomPos.string = ""
            roomStateSeenUser.string = "WAITING STATE"


            for (play <- playerNames)
              println("condition of play names ON DELETE  " + play.string)
          }
        }
      }
      case None => selected_box = boxes.find(p => withinX(p) && withinY(p))
    }
    def withinX(p: DynaBox): Boolean = (m.x < p.coord.x + p.box_width - p.box_width / 2) && (m.x > p.coord.x - p.box_width / 2)
    def withinY(p: DynaBox): Boolean = m.y < p.coord.y + p.box_height - p.box_height / 2 && m.y > p.coord.y - p.box_height / 2
  }


    key(KEY_R, onKeyDown = {
      Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.READY_STATE, " - READY")
    })
    key(KEY_W, onKeyDown = {
      Client.actorServerSelect ! SendRoomState(Client.clientActor, targetRoomNum.string.toInt, myRoomPos.string.toInt, User.WAITING_STATE, " - WAITING")
    })


    action {
      if (User.readyPlay) {
        try
          User.readyPlay = false
        finally
          MainGame.run()
      }
    }
  })

  def initMutableTexts: ListBuffer[Message] = {

    val mutableTexts = ListBuffer[Message]()
    val playerTextPosition: List[Vec] = List(Vec(220, 320), Vec(220, 300), Vec(220, 280), Vec(220, 260))
    val playerStatsTextPosition: List[Vec] = List(Vec(100, 320), Vec(100, 300), Vec(100, 280), Vec(100, 260))
    for (pos <- 0 until maxPlayerInRoom) {
      mutableTexts += MutableMessage(playerNames(pos), playerTextPosition(pos))
      mutableTexts += MutableMessage(playerRoomStats(pos), playerStatsTextPosition(pos))
    }

    mutableTexts += MutableMessage(targetRoomNum, Vec(370, 450))
    mutableTexts += MutableMessage(myRoomPos, Vec(250, 425))
    mutableTexts += MutableMessage(roomStateSeenUser, Vec(250, 405))
    for (play <- playerNames)
      println("condition of play names  " + play.string)

    return mutableTexts
  }

  def initPrefixTexts: ListBuffer[Message] = {
    val menuTextWithPos = ListBuffer[Message]()
    val menuTextPosition: List[Vec] = List(Vec(160, 450))
    menuTextWithPos += ImmutableMessage("Current room num is : \n At pos :\n and is at", menuTextPosition(0))
    menuTextWithPos += ImmutableMessage("Ready", Vec(405, 55))
    menuTextWithPos += ImmutableMessage("Go Back", Vec(75, 55))
    menuTextWithPos += ImmutableMessage("Wait", Vec(235, 55))

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

object ReadyBox extends DynaBox(Vec(430, 60), 100f, 60f, box_mass = 1000f, restitution = false) {
  render {
    drawPolygon(points, BLACK)
  }
}

object CancelBox extends DynaBox(Vec(110, 60), 100f, 60f, box_mass = 1000f, restitution = false) {
  render {
    drawPolygon(points, BLACK)
  }
}

object WaitingBox extends DynaBox(Vec(270, 60), 100f, 60f, box_mass = 1000f, restitution = false) {
  render {
    drawPolygon(points, BLACK)
  }
}