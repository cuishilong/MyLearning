package stream.demos

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

/**
 * flink的socket sink测试
 * 1、开启socket监听（①nc -l 8887,②nc -l 8888）
 * 2、开启流
 */
object FlinkSinkSocketDemo {
  val stream_tag = this.getClass.getSimpleName

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream = env.socketTextStream("localhost", 8887)

    val serde = new SimpleStringSchema()


    stream
      .map(line => line + "\n")
      .writeToSocket("localhost", 8888, serde)

    env.execute(stream_tag)
  }
}
