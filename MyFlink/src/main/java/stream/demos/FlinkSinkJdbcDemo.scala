package stream.demos

import java.sql.PreparedStatement

import org.apache.flink.connector.jdbc.internal.options.JdbcOptions
import org.apache.flink.connector.jdbc.{JdbcSink, JdbcStatementBuilder}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkSinkJdbcDemo {
  val stream_tag = this.getClass.getSimpleName

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val stream = env.socketTextStream("localhost", 8887)

    /**
     * 构造jdbc sink
     */
    val insert_sql = "insert into books (id, title, author, price, qty) values (?,?,?,?,?)"
    val stmt = new JdbcStmtBuider()
    val conn = JdbcOptions
      .builder()
      .setDBUrl("")
      .setDriverName("")
      .setUsername("")
      .setPassword("")
      .setTableName("")
      .build()
    val jdbcSink = JdbcSink.sink(insert_sql, stmt, conn)

    stream
      .addSink(jdbcSink)

    env.execute(stream_tag)
  }

  case class UserBean(id: Int, name: String, age: Int)

  class JdbcStmtBuider extends JdbcStatementBuilder[UserBean] {
    //    override def accept(ppst: PreparedStatement, e: Bean): Unit = {
    //      ppst.setInt(1, e.id)
    //      ppst.setString(2, e.name)
    //      ppst.setInt(3, e.age)
    //    }
    override def accept(t: PreparedStatement, u: UserBean): Unit = {


    }
  }



}
