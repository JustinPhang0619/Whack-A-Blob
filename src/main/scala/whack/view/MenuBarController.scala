package whack.view
import scalafxml.core.macros.sfxml
import whack.MainApp

@sfxml
class MenuBarController(){
  def handleClose(): Unit = {
    MainApp.stage.close()
  }
  def handleHelp(): Unit = {
    MainApp.showInstructions()
  }
}