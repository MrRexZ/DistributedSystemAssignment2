/**
  * Created by Mr_RexZ on 11/17/2016.
  */

import com.github.dunnololda.scage.ScageLib._

object HelloWorldExample extends ScageScreenApp("Scage App", 640, 480) {
  private var ang = 0f
  actionStaticPeriod(100) {
    ang += 5
  }

  backgroundColor = BLACK
  render {
    openglMove(windowSize / 2)
    openglRotate(ang)
    print("Hello World!", Vec(-50, -5), GREEN)
  }
}