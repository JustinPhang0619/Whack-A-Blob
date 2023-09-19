package whack.model

import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

class CustomCircle(circleRadius: Double, color: Color, val score: Int) extends Circle {
  radius = circleRadius
  fill = color
  def getCircleRadius: Double = circleRadius
}

class Small extends CustomCircle(circleRadius = 10.0, color = Color.Red, score = 30)
class Medium extends CustomCircle(circleRadius = 15.0, color = Color.Green, score = 20)
class Big extends CustomCircle(circleRadius = 20.0, color = Color.Blue, score = 10)
class Penalty extends CustomCircle(circleRadius = 15.0, color = Color.Black, score = -20)


