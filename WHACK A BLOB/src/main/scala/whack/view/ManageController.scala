package whack.view

import whack.MainApp
import whack.model.HighScore
import scalafx.scene.control.{Alert, TableColumn, TableView}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import scalafx.scene.control.Alert.AlertType


@sfxml
class ManageController(
                        private val scoreTable: TableView[HighScore],
                        private val userNameColumn: TableColumn[HighScore,String],
                        private val scoreColumn: TableColumn[HighScore, Int],
                        private val gameDurationColumn: TableColumn[HighScore,Int],
                        private val clickTimeoutColumn: TableColumn[HighScore,Int],
                        private val difficultyColumn: TableColumn[HighScore,Boolean]
                        ) {
  // initialize table
  scoreTable.items = MainApp.scoreData
  userNameColumn.cellValueFactory = data  => data.value.userName
  scoreColumn.cellValueFactory = data => data.value.score
  gameDurationColumn.cellValueFactory = data => data.value.gameDuration
  clickTimeoutColumn.cellValueFactory = data => data.value.clickTimeout
  difficultyColumn.cellValueFactory = data => data.value.difficulty

  def goHome(): Unit = {
    MainApp.showHome()
  }

  // delete selected high score if any is selected
  def handleDelete() = {
    val selectedIndex = scoreTable.selectionModel.value.selectedIndex.value
    if (selectedIndex >= 0) {
      val highScore = scoreTable.items().remove(selectedIndex)
      highScore.delete()
    } else {
      val alert = new Alert(AlertType.Warning) {
        initOwner(MainApp.stage)
        title = "No Selection"
        headerText = "No Score Selected"
        contentText = "Please select a High Score in the table."
      }.showAndWait()
    }
  }

}




