package whack.model

import whack.util.Database
import scalafx.beans.property.{ObjectProperty, StringProperty}
import scalikejdbc._

import scala.util.Try

class HighScore(val userNameS: String, val scoreS: Int, val gameDurationS: Int, val clickTimeoutS: Int,
                val difficultyS: Boolean) extends Database {
  def this() = this(null, 0, 0, 0, false)

  var userName = new StringProperty(userNameS)
  var score = new ObjectProperty[Int](this, "score", scoreS)
  var gameDuration = new ObjectProperty[Int](this, "gameDuration", gameDurationS)
  var clickTimeout = new ObjectProperty[Int](this, "clickTimeout", clickTimeoutS)
  var difficulty = new ObjectProperty[Boolean](this, "difficulty", difficultyS)

  def isExist: Boolean = {
    DB readOnly { implicit session =>
      sql"""
        select * from HighScore where userName = ${userName.value} and score = ${score.value} and gameDuration = ${gameDuration.value} and clickTimeout = ${clickTimeout.value} and difficulty = ${difficulty.value}
      """.map(rs => rs.string("userName")).single.apply()
    } match {
      case Some(x) => true
      case None => false
    }
  }

  def save(): Try[Int] = {
    if (!isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
          insert into highScore (userName, score,
          gameDuration, clickTimeout, difficulty) values
            (${userName.value}, ${score.value}, ${gameDuration.value},
              ${clickTimeout.value},${difficulty.value})
        """.update.apply()
      })
    } else {
      Try(DB autoCommit { implicit session =>
        sql"""
        update highScore
        set
        userName = ${userName.value},
        score = ${score.value},
        gameDuration = ${gameDuration.value},
        clickTimeout = ${clickTimeout.value},
        difficulty = ${difficulty.value}
         where userName = ${userName.value} and score = ${score.value} and gameDuration = ${gameDuration.value} and
         clickTimeout = ${clickTimeout.value} and difficulty = ${difficulty.value}
        """.update.apply()
      })
    }

  }

  def delete(): Try[Int] = {
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        sql"""
        delete from highScore where
          userName = ${userName.value} and
          score = ${score.value} and
          gameDuration = ${gameDuration.value} and
          clickTimeout = ${clickTimeout.value} and
          difficulty = ${difficulty.value}
        """.update.apply()
      })
    } else
      throw new Exception("User not Exists in Database")
  }

}

object HighScore extends Database {
  def apply(userNameS: String,
            scoreS: Int,
            gameDurationS: Int,
            clickTimeoutS: Int,
            difficultyS: Boolean): HighScore = {
    new HighScore(userNameS, scoreS, gameDurationS, clickTimeoutS, difficultyS) {
      userName.value = userNameS
      score.value = scoreS
      gameDuration.value = gameDurationS
      clickTimeout.value = clickTimeoutS
      difficulty.value = difficultyS
    }
  }

  def initializeTable() = {
    DB autoCommit { implicit session =>
      sql"""
        create table highScore (
          userName varchar(64) not null primary key,
          score int not null,
          gameDuration int not null,
          clickTimeout int not null,
          difficulty boolean not null
        )
      """.execute.apply()
    }
  }

  def getAllHighScore(): List[HighScore] = {
    DB readOnly { implicit session =>
      sql"select * from highScore".map(rs => HighScore(
        rs.string("userName"),
        rs.int("score"),
        rs.int("gameDuration"),
        rs.int("clickTimeout"),
        rs.boolean("difficulty")
      )).list.apply()
    }
  }

}



