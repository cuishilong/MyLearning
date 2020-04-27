package stream

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer010, FlinkKafkaProducer010}
import org.apache.flink.streaming.connectors.kafka.internals.KafkaTopicPartition

import scala.collection.JavaConversions._

object FlinkStreamWithKafkaDemo {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)

    /**
     * flink检查点相关配置
     */
    // 检查点保存位置flink-conf.yaml 进行配置 state.checkpoints.dir: hdfs://namenode-host:port/flink-checkpoints
    env.enableCheckpointing(1000,CheckpointingMode.EXACTLY_ONCE)
    val ckpConf = env.getCheckpointConfig
    // 检查点模式exactly-once or at-least-once
    ckpConf.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    // 检查点保存时间间隔
    ckpConf.setCheckpointInterval(100)
    // 设置检查点超时
    ckpConf.setCheckpointTimeout(60 * 1000)
    // 配置检查点并行个数
    ckpConf.setMaxConcurrentCheckpoints(10)

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    // only required for Kafka 0.8
    properties.setProperty("zookeeper.connect", "localhost:2181")
    properties.setProperty("group.id", "test")
    val topic = "test"
    val kafkaConsumer010 = new FlinkKafkaConsumer010[String](topic, new SimpleStringSchema(), properties)

    /**
     * 关于读取kafka数据offset的相关设置
     */
    // 从checkpoint读取offset起始位置
    kafkaConsumer010.setStartFromGroupOffsets()
    // 从一个时间点开始读取数据
    kafkaConsumer010.setStartFromTimestamp(15400)
    // 从起始点offset读取数据
    kafkaConsumer010.setStartFromEarliest()
    // 读取最新数据
    kafkaConsumer010.setStartFromLatest()
    // 从指定的offset读取数据
    val specificOffsets = Map[KafkaTopicPartition, java.lang.Long](new KafkaTopicPartition("test", 0) -> 0L, new KafkaTopicPartition("test", 1) -> 0L)
    kafkaConsumer010.setStartFromSpecificOffsets(specificOffsets)

    kafkaConsumer010.setCommitOffsetsOnCheckpoints(true)
    val dataStream = env.addSource(kafkaConsumer010)

    //    dataStream.print()

    val wordCount = dataStream
      .flatMap(_.toLowerCase().split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

//    wordCount.addSink()

    wordCount.print()

    env.execute(getClass.getSimpleName.replaceAll("\\$", ""))
  }
}
