package stream.demos

import java.lang
import java.util.Properties

import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.producer.ProducerRecord

object FlinkSinkKafkaDemo {
  val stream_tag = this.getClass.getSimpleName
  val kafka_servers = "192.168.200.44:6667,192.168.200.45:6667,192.168.200.46:6667"
  val topic = "test"

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream = env.socketTextStream("localhost", 8887)

    /**
     * kafka procuder 配置信息
     */
    val serde = new MyStringSerDe(topic)
    val prop = new Properties()
    prop.setProperty("bootstrap.servers", kafka_servers)
    val semantic = FlinkKafkaProducer.Semantic.EXACTLY_ONCE
    val kafkaSink = new FlinkKafkaProducer[String](
      topic
      , serde
      , prop
      , semantic
    )

    stream
      .map(e => e)
      .addSink(kafkaSink)

    env.execute(stream_tag)
  }

  /**
   * 自定义Flink kafka sink
   *
   * @param topic
   */
  class MyStringSerDe(topic: String) extends KafkaSerializationSchema[String] {
    override def serialize(element: String, timestamp: lang.Long): ProducerRecord[Array[Byte], Array[Byte]] = {
      new ProducerRecord(topic, null, element.getBytes())
    }
  }

}
