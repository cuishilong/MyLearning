package consumer

import java.time.Duration
import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer

import scala.collection.JavaConversions._

object KafkaConsumerDemo {
  def main(args: Array[String]): Unit = {
    val kafkaProp = new Properties()
    kafkaProp.setProperty("bootstrap.servers", "192.168.200.44:2181,192.168.200.44:2181,192.168.200.44:2181")
    kafkaProp.setProperty("group.id", "test_group")
    kafkaProp.setProperty("enable.auto.commit", "true")
    kafkaProp.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaProp.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    val consumer = new KafkaConsumer[String, String](kafkaProp)

    val topic_arr = List[String]("test")
    consumer.subscribe(topic_arr)

    while (true) {
      val record = consumer.poll(Duration.ofSeconds(1))
      if (!record.isEmpty) {
        record.iterator().foreach(e => {
          println(s"consumer : ${e}")
        })
      }
    }
  }

}

