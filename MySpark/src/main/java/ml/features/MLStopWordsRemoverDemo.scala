package ml.features

import org.apache.spark.ml.feature.{StopWordsRemover, Tokenizer}
import org.apache.spark.sql.SparkSession

object MLStopWordsRemoverDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val dataInit = Seq(
      "spark hadoop hive kafka kylin flink hbase flume sqoop zeppelin zookeeper scala java python redis mysql to is"
      , "spark hive hadoop flink kafka kylin flume" + " to is"
      , "spark hive hadoop flink kafka kylin flume" + " to is"
      , "spark spark spark flink flink flink" + " to is"
      , "spark spark spark flink flink flink" + " to is"
      , "spark spark spark flink flink flink" + " to is"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive" + " to is"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive" + " to is"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive" + " to is"
      , "kafka spark flink hadoop hive kafka spark flink hadoop hive" + " to is"
    )

    val data = spark.createDataFrame(dataInit.map(Tuple1.apply)).toDF("text")

    val data_tokenizer = new Tokenizer()
      .setInputCol("text")
      .setOutputCol("words")
      .transform(data)

    val stop_words_remover = new StopWordsRemover()
      .setInputCol("words")
      .setOutputCol("words_remover")
      .setStopWords(Array("hadoop", "spark", "hive"))

    stop_words_remover
      .transform(data_tokenizer)
      .show(false)

    spark.stop()
  }
}
