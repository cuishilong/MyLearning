package test

import util.ScalaUtil

object JdbcPpstDemo {
  def main(args: Array[String]): Unit = {
    val conn = ScalaUtil.getMysqlConn()
    conn.setAutoCommit(false)
    val sql = "insert into user(id,name) values(?,?)"
    val ppst = conn.prepareStatement(sql)
    for (i <- 1 to 1 * 10000) {
      ppst.setInt(1, i)
      ppst.setString(2, "Arica")
      ppst.addBatch()
    }

    this.synchronized{

    }

    ppst.executeBatch()
    conn.commit()
    ppst.close()
    conn.close()
  }
}
