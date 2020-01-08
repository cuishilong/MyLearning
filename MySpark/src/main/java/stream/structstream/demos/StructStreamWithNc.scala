package stream.structstream.demos

import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 1、append模式
 * 2、update模式
 * 3、complete模式
 */
object StructStreamWithNc {
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val master = "local[*]"

  case class Bean(id: Int, name: String, offset: Long, timestamp: Long)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(stream_tag)
      .master(master)
      .getOrCreate()

    val df = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .load()


    doHandleCompleteMode(df, spark)
//    doHandleAppendMode(df, spark)
//    doHandleUpdateMode(df, spark)

    spark.stop()
  }

  def doHandleCompleteMode(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .as[String]
      .flatMap(_.split(" "))
//      .groupBy("value")
//      .count()
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .start()

    query.awaitTermination()
  }

  def doHandleAppendMode(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .as[String]
      .flatMap(_.split(" "))
      //      .groupBy("value")
      //      .count()
      .writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .start()

    query.awaitTermination()
  }

  def doHandleUpdateMode(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .as[String]
      .flatMap(_.split(" "))
      //      .groupBy("value")
      //      .count()
      .writeStream
      .outputMode(OutputMode.Update())
      .format("console")
      .start()

    query.awaitTermination()
  }

}
