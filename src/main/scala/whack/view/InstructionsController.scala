package whack.view

import scalafxml.core.macros.sfxml
import whack.MainApp


@sfxml
class InstructionsController() {
  // go back to home screen
  def goBack(): Unit = {
    MainApp.showHome()
  }
}