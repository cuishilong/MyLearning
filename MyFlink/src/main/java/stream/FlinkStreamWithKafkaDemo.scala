package stream

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

object FlinkStreamWithKafkaDemo {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)
    env.enableCheckpointing(5000,CheckpointingMode.EXACTLY_ONCE)
    val ckpConf = env.getCheckpointConfig
    ckpConf.getCheckpointingMode

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "localhost:9092")
    // only required for Kafka 0.8
    properties.setProperty("zookeeper.connect", "localhost:2181")
    properties.setProperty("group.id", "test")
    val topic = "test"
    val kafkaConsumer010 = new FlinkKafkaConsumer010[String](topic, new SimpleStringSchema(), properties)
    kafkaConsumer010.setStartFromLatest()
    val dataStream = env.addSource(kafkaConsumer010)

    val cnt = dataStream
      .flatMap(_.toLowerCase().split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    cnt.print()

    env.execute(getClass.getSimpleName.replaceAll("\\$", ""))
  }
}
