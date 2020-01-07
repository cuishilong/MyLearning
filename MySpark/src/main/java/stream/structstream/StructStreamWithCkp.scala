package stream.structstream

import com.alibaba.fastjson.{JSONArray, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

object StructStreamWithCkp {
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
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      // 订阅主题
      .option("subscribe", "test")
      .option("startingOffsets", getStartingOffsets())
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "timestamp")
      .as[(String, String, Long)]

    val query = df.writeStream
      .outputMode(OutputMode.Append())
      .format("console")
      .start()

    query.awaitTermination()

    spark.stop()
  }

  def getStartingOffsets(): String = {
    val jsonObj = new JSONObject()
    val topic = "test"
    val Array(partition, offset) = Array(0, 150000)
    val jsonValue = new JSONObject()
    jsonValue.put(partition.toString, offset)
    jsonObj.put(topic, jsonValue)
    println(jsonObj.toString())
    jsonObj.toString()
  }
}
