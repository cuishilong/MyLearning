package ml.features.extract

import org.apache.spark.ml.feature.{CountVectorizer, StopWordsRemover, Tokenizer}
import org.apache.spark.sql.SparkSession

/**
 * 使用CountVectorizer方法抽取特征（适用于文本特征数据）
 * 1、分词
 * 2、去停用词
 * 3、训练抽取模型
 * 4、抽取特征
 */
object MLCountVectorizerExtractDemo {
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

    val data = spark.createDataFrame(dataInit.map(Tuple1.apply)).toDF("line")

    val tokenizer = new Tokenizer()
      .setInputCol("line")
      .setOutputCol("words")

    val data_tokenizer = tokenizer.transform(data)

    val data_remover = new StopWordsRemover()
      .setInputCol("words")
      .setOutputCol("words_remover")
      .transform(data_tokenizer)

    val count_vector = new CountVectorizer()
      .setInputCol("words_remover")
      .setOutputCol("features")

    val count_vector_model = count_vector.fit(data_remover)

    count_vector_model.transform(data_remover).show(false)

    spark.stop()
  }

}
