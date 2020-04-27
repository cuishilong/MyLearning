package sql.sql.udf

import org.apache.spark.sql.SparkSession

object SparkUdafDemo {
  val spark = SparkSession
    .builder()
    .appName(this.getClass.getSimpleName)
    .master("local")
    .getOrCreate()

  spark.udf.register("", MyUdaf)


  spark.stop()
}
