package com.sunway.screen.menu

import javax.swing.JOptionPane

import akka.pattern.ask
import akka.util.Timeout
import com.github.dunnololda.scage.ScageLib._
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.MenuActorMessages._
import com.sunway.util.{ImmutableMessage, Message, MutableMessage}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.reflect.runtime.universe._

/**
  * Created by Mr_RexZ on 11/17/2016.
  */

object MainMenu extends ScageScreen("Scage App") {

  implicit val timeout = Timeout(5.seconds)

  backgroundColor = BLACK
  val createRoomText = ImmutableMessage("Create Room", Vec(-50, 35))
  val joinRoomText = ImmutableMessage("Join Room", Vec(-50, -35))
  val editNameText = ImmutableMessage("Press F1 to edit name", Vec(200, 210))
  var playerNameText = MutableMessage(myName, Vec(-320, -240))
  var stringList = ListBuffer[Message](createRoomText, joinRoomText, playerNameText, editNameText)
  private var selected_box: Option[DynaBox] = None
  private val boxes = ArrayBuffer[DynaBox](
    CreateRoomBox,
    JoinRoomBox)

  renderTextFunctions

  key(KEY_F1, onKeyDown = Future {
    myName.string = JOptionPane.showInputDialog("Input USERNAME here")
  })

  key(KEY_F2, onKeyDown = Future {
    myPassword.string = JOptionPane.showInputDialog("Input PASSWORD here")
  })

  key(KEY_F3, onKeyDown = Future {
    targetRoomNum.string = JOptionPane.showInputDialog("Input TARGET ROOM NUM here")
  })

  key(KEY_F, onKeyDown = {
    connectAsHost
  })
  key(KEY_I, onKeyDown = {
    connectAsParticipant
  })


  def renderTextFunctions {
    render {
      openglMove(windowSize / 2)
      stringList foreach (obj => print(obj.getMessage, obj.getVectorPos, GREEN))
    }
  }

  def connectAsHost {

    val hostFuture = (Client.actorServerSelect ? SendRequestCreateRoom(Client.clientActor, myName.string, myPassword.string))
    val serverReply = Await.result(hostFuture, 3 seconds).asInstanceOf[ServerReply]
    checkType(AcceptPlayerAsHost, serverReply)
  }

  //TODO Optimize this part!!
  def checkType[T: TypeTag](obj: T, serverReply: ServerReply) = {

    if (serverReply.isInstanceOf[RejectPlayer]) JOptionPane.showMessageDialog(null, serverReply.asInstanceOf[RejectPlayer].reason);

    else {

      if (serverReply.isInstanceOf[AcceptPlayerAsHost]) Client.clientActor ! serverReply.asInstanceOf[AcceptPlayerAsHost]
      else if (serverReply.isInstanceOf[AcceptPlayerAsParticipant]) Client.clientActor ! serverReply.asInstanceOf[AcceptPlayerAsParticipant]
      RoomMenu.run()
    }

  }

  def connectAsParticipant {
    val participantFuture = Client.actorServerSelect ? SendRequestJoin(Client.clientActor, targetRoomNum.string.toInt, myName.string, myPassword.string)
    val serverReply = Await.result(participantFuture, 3 seconds).asInstanceOf[ServerReply]
    checkType(AcceptPlayerAsParticipant, serverReply)
  }

  def availableSlot(roomNum: Int): Boolean = {
    val statusGroup = Client.actorServerSelect ? AskNumOfParticipants(roomNum, Client.clientActor)
    Await.result(statusGroup, 3 seconds).asInstanceOf[Boolean]
  }

  leftMouse(onBtnDown = { m => {
    selected_box = boxes.find(p => withinX(p) && withinY(p))
    selected_box match {
      case Some(box) => {
        if (box.eq(CreateRoomBox)) {
          connectAsHost
        }
        else if (box.eq(JoinRoomBox)) {
          connectAsParticipant
        }
      }
      case None => selected_box = boxes.find(p => withinX(p) && withinY(p))
    }
    def withinX(p: DynaBox): Boolean = (m.x < p.coord.x + p.box_width - p.box_width / 2) && (m.x > p.coord.x - p.box_width / 2)
    def withinY(p: DynaBox): Boolean = m.y < p.coord.y + p.box_height - p.box_height / 2 && m.y > p.coord.y - p.box_height / 2
  }
  })


  object CreateRoomBox extends DynaBox(Vec(330, 280), 190f, 60f, box_mass = 1000f, restitution = false) {
    render {
      drawPolygon(points, BLACK)
    }
  }

  object JoinRoomBox extends DynaBox(Vec(330, 200), 190f, 60f, box_mass = 1000f, restitution = false) {
    render {
      drawPolygon(points, BLACK)
    }
  }

}








