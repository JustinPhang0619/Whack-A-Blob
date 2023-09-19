package whack.view

import scalafxml.core.macros.sfxml
import whack.MainApp

@sfxml
class HomeController() {

  def getInstructions(): Unit = {
    MainApp.showInstructions()
  }

  def getGame(): Unit = {
    MainApp.showShootingGame()
  }

  def getManage(): Unit = {
    MainApp.showManage()
  }

  def quit(): Unit = {
    MainApp.stage.close()
  }
}