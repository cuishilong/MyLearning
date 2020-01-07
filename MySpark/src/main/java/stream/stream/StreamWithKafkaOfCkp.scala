package stream.stream

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import stream.listener.MyStreamListener

import scala.collection.JavaConversions._

/**
 * 1、从kafka获取流数据
 * 2、将处理数据offsets存储到redis
 */
object StreamWithKafkaOfCkp {
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val redis_offsets_key = s"${stream_tag}_offsets_20200107"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setAppName(stream_tag)
      .setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.addStreamingListener(new MyStreamListener(stream_tag))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("test")

    val fromOffsets = getLatestOffsetsFromRedis(redis_offsets_key)

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams, fromOffsets)
    )

    stream
      .foreachRDD(rdd => {
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        saveOffsets2Redis(offsetRanges)
      })

    ssc.start()
    ssc.awaitTermination()
  }

  def saveOffsets2Redis(offsets: Array[OffsetRange]): Unit = {
    //offset:{topic-partition1:offset1,topic-partition2:offset}
    val jedis = new Jedis("localhost", 6379)
    val jsonObj = new JSONObject()
    offsets
      .foreach(offset => {
        val topic = offset.topic
        val partition = offset.partition
        val untilOffset = offset.untilOffset
        val fromOffset = offset.fromOffset
        jsonObj.put(s"${topic}-${partition}", untilOffset)
        println(s"fromOffset : ${fromOffset}")
      })
    val redisValue = jsonObj.toString()
    println(s"${redis_offsets_key} : ${redisValue}")
    jedis.set(redis_offsets_key, redisValue)
    jedis.close()
  }

  def getLatestOffsetsFromRedis(redisKey: String): Map[TopicPartition, Long] = {
    val jedis = new Jedis("localhost", 6379)
    val offsets = scala.collection.mutable.HashMap[TopicPartition, Long]()
    val jsonObj = JSON.parseObject(jedis.get(redisKey))
    val keys = jsonObj.keySet()
    keys
      .foreach(key => {
        val tmp = key.split("-")
        val topic = tmp.apply(0)
        val partition = tmp.apply(1).toInt
        val offset = jsonObj.getLong(key)
        offsets += ((new TopicPartition(topic, partition), offset))
      })
    offsets.toMap
  }
}
