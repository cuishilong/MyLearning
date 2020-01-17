package util

import java.io.FileReader
import java.sql.{Connection, DriverManager}
import java.util.Properties

object ScalaUtil {
  def main(args: Array[String]): Unit = {
    println(getConf("mysql.url"))
  }

  def getCurrentDir: String = System.getProperty("user.dir")

  def getResourceDir: String = getClass.getResource("/").getPath

  def getConf(key: String): String = {
    val path = getResourceDir + "spark.conf"
    val prop = new Properties()
    prop.load(new FileReader(path))
    prop.getProperty(key)
  }

  def getDesktopDir = "/Users/arica/Desktop"

  def getMysqlConn(url: String, user: String, password: String): Connection = {
    DriverManager.getConnection(url, user, password)
  }

  def getMysqlConn(): Connection = {
    val url = ScalaUtil.getConf("mysql.url")
    val user = ScalaUtil.getConf("mysql.user")
    val password = ScalaUtil.getConf("mysql.password")
    DriverManager.getConnection(url, user, password)
  }

}
