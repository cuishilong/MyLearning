package stream

import scala.collection.JavaConversions._

object Test {
  def main(args: Array[String]): Unit = {
    val map = scala.collection.mutable.HashMap[String, Int](
      ("id", 1)
      ,("age", 1)
    )
    //    map.put("id", 0)
    //    map.put("age", 0)


    println(map.toString())
  }
}
