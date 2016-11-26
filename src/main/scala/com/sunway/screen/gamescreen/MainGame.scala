package com.sunway.screen.gamescreen

import com.github.dunnololda.mysimplelogger.MySimpleLogger
import com.github.dunnololda.scage.ScageLib._
import com.github.dunnololda.scage.support.physics.ScagePhysics
import com.sunway.model.User
import com.sunway.model.User._

/**
  * Created by Mr_RexZ on 11/24/2016.
  */
object MainGame extends ScageScreen("Main Screen") {


  val physics = ScagePhysics(gravity = Vec(0, -50))
  val myChar = User.charactersPos(myRoomPos.string.toInt)
  private val log = MySimpleLogger(this.getClass.getName)
  private var uke_speed = 30
  private var farthest_coord = Vec.zero


  charactersPos = Array(new Character(Vec(20, windowHeight / 2 - 70), 0), new Character(Vec(150, windowHeight / 2 - 70), 1))
  private var lost = false

  physics.addPhysical(myChar)
  for (otherChar <- User.charactersPos) {
    if (!otherChar.equals(myChar)) physics.addPhysical(otherChar)
  }

  def ukeSpeed = uke_speed

  def setDefaultSpeed() {
    uke_speed = 0
  }

  def loseCondition = {
    farthest_coord.y - myChar.coord.y > windowHeight
  }

  init {
    lost = false
    farthest_coord = LevelCreator.continueLevel(Vec(0, windowHeight / 2 - 100), 0, 1000)

    val action_id = action {
      if (loseCondition) {
        log.info("uke.velocity = " + myChar.velocity)
        lost = true
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

  action {
    physics.step()

    if (farthest_coord.x - myChar.coord.x < 1000) {
      log.info("adding new platforms..." + myChar.coord)
      farthest_coord = LevelCreator.continueLevel(farthest_coord, 0, 1000)
    }
  }

  actionStaticPeriod(5000) {
    if (uke_speed < 50) {
      log.info("increasing speed:" + uke_speed)
      uke_speed += 3
    } else deleteSelfNoWarn()
  }

  backgroundColor = WHITE
  //center = myChar.coord + Vec(windowWidth / 2 - 50, 0)
  center = myChar.coord
  keyIgnorePause(KEY_F2, onKeyDown = {
    restart()
    pauseOff()
  })

  keyIgnorePause(KEY_P, onKeyDown = if (!lost) switchPause())
  keyIgnorePause(KEY_Q, onKeyDown = if (keyPressed(KEY_LCONTROL) || keyPressed(KEY_RCONTROL)) stopApp())

  interface {
    print("Z to jump (Z twice to double jump)\n" +
      "X to break obstacles\n" +
      "Down Arrow to fast down\n" +
      "P to pause current game\n" +
      "F2 to start the new one\n\n" +
      "Press P", 20, windowHeight - 20)
    if (!onPause) {
      interface {
        print(myChar.coord.ix / 100, 20, windowHeight - 20)
        if (onPause) {
          if (lost) print("Oops! You smashed to death (press F2 to start new game)", 20, windowHeight - 40)
          else print("Pause (Press P)", windowWidth / 2, windowHeight / 2)
        }
      }
      deleteSelfNoWarn()
    }
  }

  pause()


}
