package sql.read

import java.util.Properties

import org.apache.spark.sql.SparkSession
import util.ScalaUtil

object SparkReadJdbcDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val url = ScalaUtil.getConf("mysql.url")
    val prop = new Properties()
    prop.put("user", ScalaUtil.getConf("mysql.user"))
    prop.put("password", ScalaUtil.getConf("mysql.password"))
    val table = "user"

    spark
      .read
      .jdbc(url, table, prop)
      .show(false)

    spark.stop()
  }
}
