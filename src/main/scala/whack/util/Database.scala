package whack.util

import whack.model.HighScore
import scalikejdbc._

trait Database {
  val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  val dbURL = "jdbc:derby:myDB;create=true;";
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "me", "mine")
  implicit val session = AutoSession
}

object Database extends Database {
  def setupDB() = {
    if (!hasDBInitialize)
      HighScore.initializeTable()
  }

  def hasDBInitialize: Boolean = {
    DB getTable "HighScore" match {
      case Some(x) => true
      case None => false
    }

  }
}