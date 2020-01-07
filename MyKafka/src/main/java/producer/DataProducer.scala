package producer

import java.util.{Properties, Random}

import com.alibaba.fastjson.JSONObject
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object DataProducer {
  def main(args: Array[String]): Unit = {
    // Kafka 参数信息
    val kafkaprop = new Properties()
    kafkaprop.setProperty("bootstrap.servers", "localhost:9092")
    kafkaprop.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaprop.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](kafkaprop)
    val random = new Random()

    var i = 0
    while (true) {
      val jsonObj = new JSONObject()
      jsonObj.put("id", i)
      jsonObj.put("name", "arica")
      val record = new ProducerRecord[String, String]("test", 0, System.currentTimeMillis(), s"${i}", jsonObj.toString())
      producer.send(record)
      println(record.toString)
      val interval = random.nextInt(10)
      Thread.sleep(interval * 10)
      i += 1
    }
  }

}
