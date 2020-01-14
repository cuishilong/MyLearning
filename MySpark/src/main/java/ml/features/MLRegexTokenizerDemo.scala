package ml.features

import org.apache.spark.ml.feature.{RegexTokenizer, Tokenizer}
import org.apache.spark.sql.SparkSession

object MLRegexTokenizerDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val dataInit = Seq(
      "kafka spark flink hadoop hive kafka spark flink hadoop hive"
      , "kafka,spark,flink,hadoop,hive,kafka,spark,flink,hadoop,hive"
    )

    val data = spark.createDataFrame(dataInit.map(Tuple1.apply)).toDF("text")

    val regexTokenizer = new RegexTokenizer()
      .setInputCol("text")
      .setOutputCol("words")
      .setPattern("\\W")

    regexTokenizer.transform(data).show(false)

    spark.stop()
  }
}
