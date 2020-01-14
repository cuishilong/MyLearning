package ml.features

import org.apache.spark.ml.feature.{Tokenizer, Word2Vec}
import org.apache.spark.sql.SparkSession

/**
 * 使用Word2Vec方法抽取特征（适用于文本特征数据）
 * 1、分词
 * 2、训练抽取模型
 * 3、从数据集抽取特征
 */
object MLWord2VecDemo {
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


    val word2Vector = new Word2Vec()
      .setInputCol("words")
      .setOutputCol("features")
      .setVectorSize(20)

    val word_to_vector_model = word2Vector.fit(data_tokenizer)

    word_to_vector_model.transform(data_tokenizer).show(false)

    spark.stop()
  }

}
