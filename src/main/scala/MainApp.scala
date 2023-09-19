package whack

import whack.util.Database
import whack.model.HighScore
import whack.view.{ManageController, ShootingController}
import scalafx.application.{JFXApp, Platform}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{FXMLLoader, NoDependencyResolver}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import javafx.{scene => jfxs}
import javafx.scene.layout.AnchorPane

object MainApp extends JFXApp {
  Database.setupDB()
  val scoreData = new ObservableBuffer[HighScore]()
  scoreData ++= HighScore.getAllHighScore()

  val rootResource = getClass.getResource("view/MenuBar.fxml")
  val loader = new FXMLLoader(rootResource, NoDependencyResolver)
  loader.load()
  val roots = loader.getRoot[jfxs.layout.BorderPane]

  stage = new PrimaryStage {
    title = "WHACK A BLOB"
    resizable = false
    scene = new Scene {
      root = roots: jfxs.Parent
    }
  }

  stage.setOnCloseRequest{ _ =>
    Platform.exit()
    System.exit(0)
  }

  def showInstructions(): Unit = {
    val resource = getClass.getResource("view/Instructions.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    val instructionsRoot: AnchorPane = loader.load()
    roots.setCenter(instructionsRoot)
  }

  def showHome(): Unit = {
    val resource = getClass.getResource("view/Home.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    val roots: AnchorPane = loader.load()
    this.roots.setCenter(roots)
  }

  var shootingController: Option[ShootingController#Controller] = None
  def showShootingGame(): Unit = {
    val resource = getClass.getResource("view/Shooting.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots: AnchorPane = loader.getRoot[jfxs.layout.AnchorPane]
    val control = loader.getController[ShootingController#Controller]()
    shootingController = Option(control)
    this.roots.setCenter(roots)
  }

  var manageController: Option[ManageController#Controller] = None
  def showManage() = {
    val resource = getClass.getResource("view/Manage.fxml")
    val loader = new FXMLLoader(resource, NoDependencyResolver)
    loader.load()
    val roots = loader.getRoot[jfxs.layout.AnchorPane]
    val control = loader.getController[ManageController#Controller]()
    manageController = Option(control)
    this.roots.setCenter(roots)
  }
  showHome()


}
