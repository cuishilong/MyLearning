package util

import java.io.FileReader
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

}
