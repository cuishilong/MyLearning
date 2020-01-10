package stream.stream

import com.alibaba.fastjson.JSON
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import util.ScalaUtil

object StreamPredictIfBirthForUserInfo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[*]")
      .setAppName(getClass.getSimpleName.replaceAll("\\$", ""))

    val ssc = new StreamingContext(conf, Seconds(1))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "use_a_separate_group_id_for_each_stream",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("user_info_topic")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    stream
      .foreachRDD((rdd, time) => {
        val model = LogisticRegressionModel.load(ScalaUtil.getDesktopDir + "/data/model/MLPredictBirthByBinomialLogisticRegression")
        val jsonObj = rdd.collect().map(record => JSON.parseObject(record.value()))

      })


    ssc.start()
    ssc.awaitTermination()
  }

  def jsonStr2JsonObj(jsonStr: String): Unit = {
    val jsonObj = JSON.parseObject(jsonStr)
  }
}
