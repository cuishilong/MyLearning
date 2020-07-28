package stream.demos

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

import scala.collection.JavaConversions._

object FlinkSourceKafkaDemo {
  val stream_tag = this.getClass.getSimpleName

  val kafka_servers = "192.168.200.44:6667,192.168.200.45:6667,192.168.200.46:6667"

  def main(args: Array[String]): Unit = {

    val param = ParameterTool.fromArgs(args)

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.getConfig.setGlobalJobParameters(param)
    /**
     * 配置kafka源
     */
    val prop = new Properties()
    prop.setProperty("bootstrap.servers", kafka_servers)
    prop.setProperty("group.id", stream_tag)
    val serdeScheme = new SimpleStringSchema()
    val topic = "test"
    val kafkaSource = new FlinkKafkaConsumer[String](topic, serdeScheme, prop)
    kafkaSource.setStartFromLatest()
    //    val startOffsetMap = Map[KafkaTopicPartition, java.lang.Long](
    //      (new KafkaTopicPartition("", 0), 123L)
    //    )
    //    kafkaSource.setStartFromSpecificOffsets(startOffsetMap)

    val stream = env.addSource(kafkaSource)

    stream.print()

    env.execute("Streaming Word Count")
  }
}
