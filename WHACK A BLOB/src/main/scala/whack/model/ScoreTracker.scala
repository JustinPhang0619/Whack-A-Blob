package whack.model

object ScoreTracker {
  private var currentScore: Int = 0

  def getCurrentScore: Int = currentScore

  def addToScore(points: Int): Unit = {
    currentScore += points
  }
  def resetScore(): Unit = {
    currentScore = 0
  }
}
