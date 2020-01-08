package stream.structstream.demos

import java.sql.Timestamp

import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * 3种output模式下的worldcount逻辑
 * 1、complete模式
 * 注：①必须使用聚合函数，②可以使用窗口操作，③可以使用水印操作
 * 2、update模式
 * 注：①只有update模式支持state状态操作，②
 * 3、append模式
 * 注：
 */

object WordCountWithNcByOutputMode {
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val master = "local[*]"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(stream_tag)
      .master(master)
      .getOrCreate()
    import spark.implicits._

    val df = spark
      .readStream
      .format("socket")
      .option("host", "localhost")
      .option("port", 9999)
      .option("includeTimestamp", true)
      .load()
      .as[(String, Timestamp)]
      .toDF("word", "timestamp")

    //    doHandleCompleteModeWithWindows(df, spark)
    //    doHandleCompleteModeByWatermark(df, spark)
    //    doHandleCompleteModeWithAgg(df, spark)

    doHandleAppendMode(df, spark)

    //    doHandleUpdateMode(df, spark)

    spark.stop()
  }

  def doHandleCompleteModeWithAgg(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .select("word")
      .as[String]
      .flatMap(_.split(" "))
      .groupBy("word")
      // agg(("value","count")) 等同于count
      .agg(("word", "count"))
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .start()

    query.awaitTermination()
  }

  def doHandleCompleteModeByWatermark(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .as[(String, Timestamp)]
      .flatMap(line => {
        line._1.split(" ").map((_, line._2))
      })
      .toDF("word", "timestamp")
      .withWatermark("timestamp", "10 minutes")
      .groupBy("word")
      // agg(("value","count")) 等同于count
      .agg(("word", "count"))
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .option("truncate", "false")
      .start()

    query.awaitTermination()
  }

  def doHandleCompleteModeWithWindows(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .as[(String, Timestamp)]
      .flatMap(line => {
        line._1.split(" ").map((_, line._2))
      })
      .toDF("word", "timestamp")
      .withWatermark("timestamp", "10 minutes")
      .groupBy(window($"timestamp", "10 minutes", "1 minutes"), $"word")
      // agg(("value","count")) 等同于count
      .agg(("word", "count"))
      .orderBy("window")
      .writeStream
      .outputMode(OutputMode.Complete())
      .format("console")
      .option("truncate", "false")
      .start()

    query.awaitTermination()
  }

  def doHandleAppendMode(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._
    val query = df
      .select("word")
      .as[String]
      .flatMap(_.split(" "))
      .writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .option("truncate", "false")
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
      .option("truncate", "false")
      .start()

    query.awaitTermination()
  }
}
