package com.sunway.screen.gamescreen

import com.github.dunnololda.mysimplelogger.MySimpleLogger
import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.Vec
import com.github.dunnololda.scage.support.physics.ScagePhysics
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.GameplayActorMessages._

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
object MainGame extends ScageScreen("Main Screen") {

  val physics = ScagePhysics(gravity = Vec(0, -50))
  val tracer = CoordTracer(solid_edges = false)
  private val log = MySimpleLogger(this.getClass.getName)
  private var char_speed = 30
  private var farthest_coord = Vec.zero
  val MATCH_PROGRESS = 0
  val MATCH_END = 1
  var won = false
  var lost = false
  var winningPlayer: Option[Int] = None
  var goBack = false


  preinit {
    try
      charactersObj(myRoomPos.string.toInt) = new Character(charactersPos(myRoomPos.string.toInt), myRoomPos.string.toInt)
    finally {
      Client.clientActor ! SendMyCharacterObject(myRoomPos.string.toInt, charactersObj(myRoomPos.string.toInt).coord.x, charactersObj(myRoomPos.string.toInt).coord.y)
      println("SENT MY CHARACTER OBJ")
    }

  }

  action {
    if (newPlayerJoining) createChar
    if (oldPlayerLeave) delChar(oldPlayerPos.get)
  }

  def createChar: Unit = {
    println("NEW CHARACTER JOINING")
    val newPlayerPost: Int = newPlayerPos.get
    charactersObj(newPlayerPost) = new Character(newPlayerVec, newPlayerPost)
    physics.addPhysical(charactersObj(newPlayerPost))
    newPlayerJoining = false
  }

  def delChar(oldPlayerPost: Int): Unit = {

    physics.removePhysicals(charactersObj(oldPlayerPost))
    oldPlayerLeave = false
  }

  init {
    won = false
    lost = false
    goBack = false
    winningPlayer = None
    backgroundColor = WHITE
    LevelDrawer.generatePlatformsInUser()
    val action_id = action {
      if (wonCondition) {
        won = true
        pause()
        deleteSelfNoWarn()
      }
    }
    clear {
      delOperationNoWarn(action_id)
      deleteSelfNoWarn()
    }
    Unit
  }
  clear {
    physics.removeAll()
  }

  action {
    physics.step()
  }

  def wonCondition = {
    charactersObj(myRoomPos.string.toInt).isTouching(LevelDrawer.flag)
  }

  center = charactersObj(myRoomPos.string.toInt).coord
  interface {
        if (onPause) {
          if (won) {
            print("YOU WON!!  PRESS ESC TO GO BACK!", 20, windowHeight - 40)
            Client.clientActor ! InformWinState()
            goBack = true

          }
          else if (lost) {
            print(s"You lost, player ${winningPlayer.get} won.  PRESS ESC TO GO BACK.", 20, windowHeight - 40)
            goBack = true
          }
        }
      }

  keyIgnorePause(KEY_ESCAPE, onKeyDown = {
    pauseOff()
  })

  /*  key(KEY_P, onKeyDown = {
      goBackScreen(MATCH_PROGRESS)
    })
  */
  action {
    if (goBack) {
      goBackScreen(MATCH_END)
      goBack = false
    }
    if (charactersObj(myRoomPos.string.toInt).coord.y < -50) {
      callEvent("RESET POS")
    }
  }

  onEvent("RESET POS") {
    charactersObj(myRoomPos.string.toInt).coord_=(Vec(20, windowHeight / 2 - 70))
    charactersObj(myRoomPos.string.toInt).velocity_=(Vec(0, 0))
    charactersObj(myRoomPos.string.toInt).body.setForce(0f, 0f)
    Client.clientActor ! SendCoordinatesFromMe(charactersObj(myRoomPos.string.toInt).body.getPosition.getX, charactersObj(myRoomPos.string.toInt).body.getPosition.getY)
  }

  private def goBackScreen(option: Int) {
    if (option == MATCH_PROGRESS) Client.clientActor ! RemoveMyCharacterObject()

    Client.clientActor ! ChangeMenuState(option)
    backgroundColor = BLACK
    MainGame.lost = false
    MainGame.won = false
    winningPlayer = None
    MainGame.clear()
    MainGame.stop()
  }
}
