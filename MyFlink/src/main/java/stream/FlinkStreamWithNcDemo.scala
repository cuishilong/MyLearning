package stream

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}

object FlinkStreamWithNcDemo {
  def main(args: Array[String]): Unit = {
    val params = ParameterTool.fromArgs(args)
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.setGlobalJobParameters(params)

    val dataStream = env.socketTextStream("localhost", 9999)

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
