package stream.demos

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._

/**
 * 用nc模拟socket发送消息，统计单词数
 * 1、开启nc监听，nc -l 8887
 * 2、开启流
 */

object FlinkSourceSocketDemo {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val url = "localhost"
    val port = 8887

    val stream = env.socketTextStream(url, port)

    val cnts = stream
      .flatMap(_.split(" "))
      .map(e => WordCountBean(e, 1))
      .keyBy("word")
      .sum("cnt")

    cnts.print().setParallelism(1)

    env.execute("socket wount count")
  }

  case class WordCountBean(word: String, cnt: Int)

}
