package ml.features.chinese

import com.hankcs.hanlp.tokenizer.StandardTokenizer
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.{Row, SparkSession}

import scala.collection.JavaConversions._

object StandardTokenizerWithSpark {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()
    import spark.implicits._

    val dataInit = Seq(
      (1, "南京市长江大桥")
      , (2, "你好中国")
      , (3, "北京欢迎你")
      , (4, "我的老家在范县")
    )

    val data = spark.createDataFrame(dataInit).toDF("id", "text")

    val data_hanlp = data
      .map { case Row(id: Int, text: String) =>
        val words = StandardTokenizer
          .segment(text)
          .map(e => e.word)
          .mkString(" ")
        (id, words)
      }
      .toDF("id", "words")

    val data_tokenizer = new Tokenizer()
      .setInputCol("words")
      .setOutputCol("words_tokenizer")
      .transform(data_hanlp)

    val hashingTF = new HashingTF()
      .setNumFeatures(10)
      .setInputCol("words_tokenizer")
      .setOutputCol("words_tf")
      .transform(data_tokenizer)

    val idfModel = new IDF()
      .setInputCol("words_tf")
      .setOutputCol("words_idf")
      .fit(hashingTF)

    idfModel.transform(hashingTF).show(false)

    spark.stop()
  }
}
