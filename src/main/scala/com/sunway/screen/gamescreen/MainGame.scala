package com.sunway.screen.gamescreen

import com.github.dunnololda.mysimplelogger.MySimpleLogger
import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.Vec
import com.github.dunnololda.scage.support.physics.ScagePhysics
import com.sunway.model.User._
import com.sunway.network.Client
import com.sunway.network.actors.GameplayActorMessages.{ChangeMenuState, InformWinState, SendCoordinatesFromMe}

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
object MainGame extends ScageScreen("Main Screen") {


  val physics = ScagePhysics(gravity = Vec(0, -50))
  private val log = MySimpleLogger(this.getClass.getName)
  private var uke_speed = 30
  private var farthest_coord = Vec.zero
  if (myRoomPos.string.toInt == 1) Thread.sleep(1000)
  charactersObj = Array(new Character(Vec(20, windowHeight / 2 - 70), 0), new Character(Vec(150, windowHeight / 2 - 70), 1))
  private var won = false
  var lost = false
  var winningPlayer: Option[Int] = None

  val myChar = charactersObj(myRoomPos.string.toInt)


  def ukeSpeed = uke_speed

  def setDefaultSpeed() {
    uke_speed = 0
  }

  def wonCondition = {
    myChar.isTouching(LevelDrawer.flag)
  }

  init {
    pauseOff()
    physics.addPhysical(myChar)
    for (otherChar <- charactersObj) {
      if (!otherChar.equals(myChar)) physics.addPhysical(otherChar)
    }
    backgroundColor = WHITE
    LevelDrawer.generatePlatformsInUser()
    val action_id = action {
      if (wonCondition) {
        log.info("uke.velocity = " + myChar.velocity)
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


  center = myChar.coord
  keyIgnorePause(KEY_F2, onKeyDown = {
    myChar.coord_=(Vec(20, windowHeight / 2 - 70))
    Client.clientActor ! SendCoordinatesFromMe(myChar.body.getPosition.getX, myChar.body.getPosition.getY)
    pauseOff()
  })

  interface {
    if (!onPause) {
      interface {
        print(myChar.coord.ix / 100, 20, windowHeight - 20)
        if (onPause) {
          if (won) {
            print("YOU WON!!", 20, windowHeight - 40)
            Client.clientActor ! InformWinState()
            goBackScreen
          }
          else if (lost) {
            print(s"You lost, player ${winningPlayer.get} won", 20, windowHeight - 40)
            goBackScreen

          }
        }
      }
      deleteSelfNoWarn()
    }
  }

  private def goBackScreen {
    Thread.sleep(1000)
    Client.clientActor ! ChangeMenuState()
    backgroundColor = BLACK
    MainGame.clear()
    MainGame.stop()
  }



  //pause()


}
