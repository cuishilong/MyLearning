package stream.structstream

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import stream.listener.MyStreamQueryListener
import util.DateUtil

/**
 * 1、流数据源Kafka
 * 2、使用监听器
 * 3、使用Checkpoint
 * 4、使用样例类
 * 5、使用触发器
 * 6、使用append输出模式
 */
object StructStreamWithKafkaOfCkp {
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val master = "local[*]"

  case class Bean(id: Int, name: String, offset: Long, timestamp: Long)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(stream_tag)
      .master(master)
      .getOrCreate()
    import spark.implicits._
    spark.streams.addListener(new MyStreamQueryListener(stream_tag))

    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      // 订阅主题
      .option("subscribe", "test")
      //      .option("startingOffsets", getStartingOffsets())
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "offset", "timestamp")
      .as[(String, String, Long, Long)]
      .map(e => {
        val jsonObj = JSON.parseObject(e._2)
        val id = jsonObj.getInteger("id")
        val name = jsonObj.getString("name")
        val offset = e._3
        val timestamp = e._4
        Bean(id, name, offset, timestamp)
      })
      .toDF()

    val dateStr = DateUtil.getDiffDayByToday(0, "yyyyMMddHHmmss")
    val query = df
      .writeStream
      .trigger(Trigger.ProcessingTime(1000))
      .outputMode(OutputMode.Append())
      .option("checkpointLocation", s"hdfs://localhost:9000/user/spark/ckp/${stream_tag}/${dateStr}")
      //      .option("checkpointLocation", "path/to/HDFS/dir")
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
