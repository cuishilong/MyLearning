package ml.features

import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.SparkSession

/**
 * 使用TF-IDF方法抽取特征（适用于文本特征数据）
 * 1、分词
 * 2、对term进行hashing处理
 * 3、训练IDF模型，并使用模型转换hashing处理过的数据集
 * 注：根据业务具体情况，可直接使用步骤2的特征，也可使用步骤3的特征
 */
object MLTfIdfDemo {
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

    val words_tokenizer = tokenizer.transform(data)

    val hashingTF = new HashingTF()
      .setInputCol("words")
      .setOutputCol("word_tf")
      .setNumFeatures(20)

    val words_tf = hashingTF.transform(words_tokenizer)

    val idf = new IDF()
      .setInputCol("word_tf")
      .setOutputCol("features")

    val idf_model = idf.fit(words_tf)

    val res = idf_model.transform(words_tf)

    res.show(false)

    spark.stop()
  }
}
