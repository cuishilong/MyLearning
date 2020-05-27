package stream.stream

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import stream.listener.MyStreamListener

import scala.collection.JavaConversions._

/**
 * 1、流数据源为Kafka
 * 2、offsets存储到外部存储redis
 * 3、添加监听器，处理监控问题
 */

object StreamWithKafkaOfRedisOffsets {
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val redis_offsets_key = s"${stream_tag}_offsets_20200107"

  def main(args: Array[String]): Unit = {
    val Array(brokers, groupId, topics) = args
    val conf = new SparkConf()
      .setAppName(stream_tag)
      .setMaster("local[*]")

    val ssc = new StreamingContext(conf, Seconds(1))
    ssc.addStreamingListener(new MyStreamListener(stream_tag))

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "latest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val fromOffsets = getLatestOffsetsFromRedis(redis_offsets_key)
    val topicSet = topics.split(",").toSet[String]

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams, fromOffsets)
    )

    /**
     * 关于offset手动提交问题
     * 1、用transform函数把offset先存到临时redis的key里面
     * 2、处理业务逻辑
     * 3、在输出造作结束后，取出临时offset再存到正式offset的redis的key里面
     * 4、输出造作代码块可以加锁，synchronize
     */

    stream.transform(rdd => {
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      val offsetJsonStr = offsetRange2JsonStr(offsetRanges)
      rdd
    })

    stream
      .foreachRDD(rdd => {
        this.synchronized{
          //此处为自己的业务逻辑

          // 。。。

          // 更新offset
          val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
          saveOffsets2Redis(offsetRanges)
        }
      })

    ssc.start()
    ssc.awaitTermination()
  }

  def saveOffsets2Redis(offsets: Array[OffsetRange]): Unit = {
    //offset:{topic-partition1:offset1,topic-partition2:offset}
    val jedis = new Jedis("localhost", 6379)
    val redisValue = offsetRange2JsonStr(offsets)
    println(s"${redis_offsets_key} : ${redisValue}")
    jedis.set(redis_offsets_key, redisValue)
    jedis.close()
  }

  def offsetRange2JsonStr(offsets: Array[OffsetRange]): String = {
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
    jsonObj.toString()
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
