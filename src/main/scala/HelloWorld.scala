/**
  * Created by Mr_RexZ on 11/17/2016.
  */

import com.github.dunnololda.scage.ScageLib._
import com.sunway.screen.gamescreen.MainGame

object HelloWorld extends ScageScreen("Scage App") {
  var f = 1
  private var ang = 0f
  actionStaticPeriod(100) {
    ang += 5
  }

  key(KEY_S, onKeyDown = {

    f = 0
  })
  backgroundColor = BLACK
  render {
    openglMove(windowSize / 2)
    openglRotate(ang)
    print("Hello World!", Vec(-50, -5), GREEN)
  }
  action(100) {
    if (f == 0) MainGame.run()
  }
}