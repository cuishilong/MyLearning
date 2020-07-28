package stream.demos

import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import redis.clients.jedis.Jedis

/**
 * 暂时未解决
 */
object FlinkSinkRedisDemo {
  val stream_tag = this.getClass.getSimpleName

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream = env.socketTextStream("localhost", 8887)

    stream
      .addSink(new MyRedisSink[String](new MyJedis()))

    env.execute(stream_tag)
  }

  class MyRedisSink[V](j: MyJedis) extends SinkFunction[V] {
    val jedis = j.jedis

    override def invoke(v: V): Unit = {
      jedis.sadd("test_set", v.toString)
    }
  }

  class MyJedis() extends Serializable {
    val jedis = new Jedis("192.168.200.44", 6379)
  }

}
