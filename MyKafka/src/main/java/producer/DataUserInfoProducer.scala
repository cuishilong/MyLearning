package producer

import java.util.{Properties, Random}

import com.alibaba.fastjson.JSONObject
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import util.BeanUtil.UserInfoBean
import util.DateUtil

object DataUserInfoProducer {
  def main(args: Array[String]): Unit = {
    // Kafka 参数信息
    val kafkaprop = new Properties()
    kafkaprop.setProperty("bootstrap.servers", "localhost:9092")
    kafkaprop.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    kafkaprop.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](kafkaprop)
    val random = new Random()

    val nameSeed = Map[Int, String]((0, "Arica"), (1, "Lily"), (2, "Hanlei"), (3, "Lucy"), (4, "Lina"), (5, "Anna"), (6, "Nancy"), (7, "Tom"), (8, "Trump"), (9, "cuishilong"))
    val someWord = Map[Int, String]((0, "Hello Word"), (1, "Welcome to China"), (2, "yes"), (3, "I am cuishilong"), (4, "Who are you"), (5, "no"), (6, "I am fine"), (7, "Good luck"), (8, "Sunny day"), (9, "haha"))

    val topic = "user_info_topic"
    var index = 0
    while (true) {
      val id = index
      val name = nameSeed.getOrElse(random.nextInt(10), "null")
      val age = random.nextInt(50 - 18) + 18
      val gender = random.nextInt(2)
      val degree = random.nextInt(5)
      val declare = someWord.getOrElse(random.nextInt(10), "null")
      val dayOfWeek = DateUtil.getDayOfWeek(random.nextInt(100))
      val dayOfMonth = DateUtil.getDayOfMonth(random.nextInt(100))
      val bean = if (age > 30 && declare == "yes") {
        UserInfoBean(id, 0, name, age, gender, degree, declare, dayOfWeek, dayOfMonth)
      } else {
        UserInfoBean(id, 1, name, age, gender, degree, declare, dayOfWeek, dayOfMonth)
      }
      val jsonObj = this.bean2JsonObj(bean)
      val record = new ProducerRecord[String, String](topic, 0, System.currentTimeMillis(), s"${id}", jsonObj.toString())
      producer.send(record)
      val interval = random.nextInt(10)
      Thread.sleep(interval * 10)
      index += 1
    }
  }

  def bean2JsonObj(bean: UserInfoBean): JSONObject = {
    val jsonObj = new JSONObject()
    jsonObj.put("id", bean.id)
    jsonObj.put("label", bean.label)
    jsonObj.put("name", bean.name)
    jsonObj.put("age", bean.age)
    jsonObj.put("degree", bean.degree)
    jsonObj.put("gender", bean.gender)
    jsonObj.put("declare", bean.declare)
    jsonObj.put("dayOfWeek", bean.dayOfWeek)
    jsonObj.put("dayOfMonth", bean.dayOfMonth)
    jsonObj
  }

}
