package ml.features

import org.apache.spark.ml.feature.Binarizer
import org.apache.spark.sql.SparkSession

object MLBinarizerDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val dataInit = Seq(1, 2, 3, 4, 7, 8, 9, 10).map(_.toDouble)

    val data = spark.createDataFrame(dataInit.map(Tuple1.apply)).toDF("num")

    val binarizer = new Binarizer()
      .setThreshold(6.0)
      .setInputCol("num")
      .setOutputCol("num_binarizer")

    binarizer.transform(data).show(false)

    spark.stop()
  }
}
