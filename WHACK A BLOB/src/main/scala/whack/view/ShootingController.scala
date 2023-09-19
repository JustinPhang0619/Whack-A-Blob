package whack.view

import whack.MainApp
import whack.model._
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.layout.Pane
import scalafxml.core.macros.sfxml
import scalafx.scene.text.Text
import scalafx.util.Duration
import scalafx.animation.PauseTransition
import scalafx.scene.control.{Alert, Button, ButtonType, CheckBox, DialogPane, Label, Slider, TextField}
import scalafx.scene.layout.VBox
import scalafx.stage.Modality
import javafx.scene.input.MouseEvent

import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import scala.util.Random
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.media.MediaView

import java.io.File
@sfxml
class ShootingController(private val gameArea: Pane,
                         private val gameOverLabel: Text,
                         private val scoreLabel: Text,
                         private val startButton: Button,
                         private val settingsButton: Button,
                         private val saveButton: Button,
                         private val homeButton: Button,
                         private var _highScore: HighScore,
                         private var timerText: Text
                        ) {
  //game area dimensions
  private val screenWidth = 667
  private val screenHeight = 437
  private val circleGenerationInterval = 250
  private var gameDuration: Int = 10000
  private var clickTimeout: Int = 2000
  private var gameRunning = false
  private var gameEnded = false
  private var circleGenerationTimer: Timer = _
  private var difficulty: Boolean = true
  private var executorService: ScheduledExecutorService = _
  private var remainingTime: Int = gameDuration
  private val musicFile = new File("src/main/resources/music.mp3").toURI.toString
  private val mediaPlayer = new MediaPlayer(new Media(musicFile))
  private val mediaView = new MediaView(mediaPlayer)
  mediaView.mediaPlayer() = mediaPlayer
  mediaView.visible = false
  def highScore: HighScore = _highScore
  def highScore_=(value: HighScore): Unit = {
    _highScore = value
  }
  // prevent user from clicking save button before game starts
  saveButton.setDisable(true)

  // event handlers
  private def gameDurationExecutor(): ScheduledExecutorService = {
    Executors.newSingleThreadScheduledExecutor()
  }

  // start button event handler
  private def startGame(): Unit = {
    gameRunning = true
    gameEnded = false
    ScoreTracker.resetScore()
    _highScore = new HighScore()
    gameOverLabel.visible = false
    updateScoreLabel()
    gameArea.onMouseClicked = (event: MouseEvent) => {
      event.consume()
    }
    mediaPlayer.play()
    mediaPlayer.cycleCount = MediaPlayer.Indefinite
    // start the circle generation for the game
    circleGenerationTimer = new Timer()
    circleGenerationTimer.scheduleAtFixedRate(new TimerTask() {
      override def run(): Unit = {
        if (gameRunning) {
          Platform.runLater(() => generateCircle())
        } else {
          circleGenerationTimer.cancel()
        }
      }
    }, 0, circleGenerationInterval)

    // start the game duration
    executorService = gameDurationExecutor()
    executorService.schedule(new Runnable {
      override def run(): Unit = endGame()
    }, gameDuration, TimeUnit.MILLISECONDS)
    remainingTime = gameDuration / 1000

    // start the timer text
    executorService.scheduleAtFixedRate(new Runnable {
      override def run(): Unit = updateTimerText()
    }, 0, 1, TimeUnit.SECONDS)
  }

  // method to update timer every second
  private def updateTimerText(): Unit = {
    if (remainingTime >= 0) {
      Platform.runLater(() => {
        timerText.text = s"Time: ${remainingTime}s"
      })
      remainingTime -= 1
    } else {
      Platform.runLater(() => {
        timerText.text = s"Time: 0s"
      })
    }
  }

  // method to generate circles on the screen
  private def generateCircle(): Unit = {
    if (!gameEnded) {
      // select a random CustomCircle subclass
      val circleType = getRandomCircleType()
      val radius = circleType.getCircleRadius

      val circle = circleType
      circle.centerX = Random.nextDouble() * (screenWidth - radius * 2) + radius
      circle.centerY = Random.nextDouble() * (screenHeight - radius * 2) + radius

      gameArea.children.add(circle)

      // set a flag to indicate whether the circle was clicked or not
      var circleClicked = false

      // create a PauseTransition for this circle
      val pauseTransition = new PauseTransition(Duration(clickTimeout))
      pauseTransition.onFinished = () => {
        if (!gameEnded && difficulty && gameArea.children.contains(circle) && !circleClicked) {
          // If the circle was not clicked, remove it from the UI
          gameArea.children.remove(circle)
        }
      }

      // set the circle's onMouseClicked event handler
      circle.onMouseClicked = (_: MouseEvent) => {
        if (gameRunning && !circleClicked) {
          ScoreTracker.addToScore(circleType.score)
          updateScoreLabel()
          circleClicked = true
          gameArea.children.remove(circle)
          circle.onMouseClicked = null
          pauseTransition.stop()
          pauseTransition.onFinished = null
        }
      }
      pauseTransition.play()
    }
  }

  // helper method to get a random CustomCircle subclass
  private def getRandomCircleType(): CustomCircle = {
    val circleTypes = Array(new Small, new Medium, new Big, new Penalty)
    val randomIndex = Random.nextInt(circleTypes.length)
    circleTypes(randomIndex)
  }

  // method to end game when completed
  private def endGame(): Unit = {
    if (!gameEnded) {
      gameRunning = false
      gameEnded = true
      mediaPlayer.stop()
      startButton.setDisable(false)
      settingsButton.setDisable(false)
      homeButton.setDisable(false)
      saveButton.setDisable(false)

      // Remove all circles from the screen
      Platform.runLater(() => {
        gameArea.getChildren.clear()
      })

      // display high score and shutdown all timers and executors
      gameOverLabel.setText(s"Game Over! \nYour Score: ${ScoreTracker.getCurrentScore}")
      gameOverLabel.visible = true
      // shutdown all 3 timers
      circleGenerationTimer.cancel()
      executorService.shutdown()
      executorService.shutdown()

    }
  }

  // update the score label only if the game is still running
  private def updateScoreLabel(): Unit = {
    if (!gameEnded) {
      scoreLabel.text = s"Score: ${ScoreTracker.getCurrentScore}"
    }
  }

  // method to save settings
  def getSettings(): Unit = {
    // create a dialog to show the settings
    val dialog = new Alert(Alert.AlertType.Information)
    dialog.setTitle("Game Settings")
    dialog.setHeaderText("Change Game Settings")
    dialog.initModality(Modality.ApplicationModal)
    val dialogPane = new DialogPane()

    // create the text fields for gameDuration and clickTimeout
    val gameDurationField = new TextField()
    gameDurationField.text = gameDuration.toString
    val clickTimeoutField = new TextField()
    clickTimeoutField.text = clickTimeout.toString

    // create a CheckBox for the difficulty level
    val difficultyCheckBox = new CheckBox("Hard Difficulty")
    difficultyCheckBox.selected = difficulty

    val volumeSlider = new Slider(0, 1, mediaPlayer.volume.value)
    volumeSlider.showTickMarks = true
    volumeSlider.showTickLabels = true
    volumeSlider.majorTickUnit = 0.25
    volumeSlider.blockIncrement = 0.1

    dialogPane.setContent(new VBox(10, new Label("Game Duration (Milliseconds):"), gameDurationField,
      new Label("Click Timeout (Milliseconds):"), clickTimeoutField, difficultyCheckBox, new Label("Music Volume:"), volumeSlider))
    dialog.dialogPane = dialogPane
    dialog.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    val result: Option[ButtonType] = dialog.showAndWait()
    result match {
      case Some(ButtonType.OK) =>
        val newGameDuration = try {
          gameDurationField.text.value.toInt
        } catch {
          case _: NumberFormatException => gameDuration // Use the current value if parsing fails
        }
        val newClickTimeout = try {
          clickTimeoutField.text.value.toInt
        } catch {
          case _: NumberFormatException => clickTimeout // Use the current value if parsing fails
        }
        val newDifficulty = difficultyCheckBox.selected.value
        val newVolume = volumeSlider.value.value

        // Uudate the settings
        gameDuration = newGameDuration
        clickTimeout = newClickTimeout
        difficulty = newDifficulty
        mediaPlayer.setVolume(newVolume)
      case _ =>
    }
  }

  // method to save score
  def saveScore(): Unit = {
    // create a dialog to save the scores
    val dialog = new Alert(Alert.AlertType.Information)
    dialog.setTitle("Game Settings")
    dialog.setHeaderText("Enter User Name!")
    dialog.initModality(Modality.ApplicationModal)
    val dialogPane = new DialogPane()

    val userNameField = new TextField()
    userNameField.text = ""

    dialogPane.setContent(new VBox(10, new Label("User Name:"), userNameField))
    dialog.dialogPane = dialogPane
    dialog.buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    val result: Option[ButtonType] = dialog.showAndWait()

    result match {
      case Some(ButtonType.OK) =>
        val newUserName = try {
          if (userNameField.text.value == "") {
            "PLAYER"
          } else {
            userNameField.text.value
          }
        } catch {
          case _: Exception => "PLAYER"
        }
        if (_highScore != null) {
          _highScore.userName.value = newUserName
          _highScore.score.value = ScoreTracker.getCurrentScore
          _highScore.difficulty.value = difficulty
          _highScore.gameDuration.value = gameDuration
          _highScore.clickTimeout.value = clickTimeout

          MainApp.scoreData += highScore
          highScore.save()
        }
      case _ =>
    }
  }

  // button to start the game
  def getStart(): Unit = {
    if (!gameRunning) {
      startButton.setDisable(true)
      settingsButton.setDisable(true)
      homeButton.setDisable(true)
      saveButton.setDisable(true)
      startGame()
    }
  }

  // button to go home
  def goHome() = {
    MainApp.showHome()
  }

}
