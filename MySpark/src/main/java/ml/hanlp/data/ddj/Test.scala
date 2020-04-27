package ml.hanlp.data.ddj

import com.hankcs.hanlp.tokenizer.StandardTokenizer
import org.apache.spark.sql.SparkSession
import util.ScalaUtil

import scala.collection.JavaConversions._

object Test {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(this.getClass.getSimpleName)
      .master("local")
      .getOrCreate()
    import spark.implicits._

    val dir = ScalaUtil.getCurrentDir

    spark.read.load()

    spark
      .read
      .textFile(s"${dir}/data/1.txt")
      .map(line => {
        val words = StandardTokenizer
          .segment(line)
          .map(e => e.word)
          .mkString(" ")
        words
      })
      .show(1000,false)

    spark.stop()
  }
}
