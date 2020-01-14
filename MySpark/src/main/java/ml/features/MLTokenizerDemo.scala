package ml.features

import org.apache.spark.ml.feature.Tokenizer
import org.apache.spark.sql.SparkSession

object MLTokenizerDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val dataInit = Seq(
      "spark hadoop hive kafka kylin flink hbase flume sqoop zeppelin zookeeper scala java python redis mysql"
      , "spark hive hadoop flink kafka kylin flume "
      , "spark hive hadoop flink kafka kylin flume "
      , "spark spark spark flink flink flink"
      , "spark spark spark flink flink flink"
      , "spark spark spark flink flink flink"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive"
    )

    val data = spark.createDataFrame(dataInit.map(Tuple1.apply)).toDF("text")

    val tokenizer = new Tokenizer()
      .setInputCol("text")
      .setOutputCol("words")

    tokenizer.transform(data).show(false)

    spark.stop()
  }
}
