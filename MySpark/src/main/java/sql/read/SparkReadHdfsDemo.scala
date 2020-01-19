package sql.read

import org.apache.spark.sql.SparkSession

object SparkReadHdfsDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    
    spark.stop()
  }
}
