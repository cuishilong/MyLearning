package sql.sql.udf

import org.apache.spark.sql.SparkSession

object SparkUdfDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName)
      .master("local")
      .getOrCreate()


    spark.udf.register("", this.getAgeFromIdCard _)


    spark.stop()
  }

  def getAgeFromIdCard(idCard: String): Int = {
    idCard.toInt
  }


}
