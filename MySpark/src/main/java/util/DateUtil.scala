package util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

object DateUtil {
  def main(args: Array[String]): Unit = {
    println(getDiffDayByToday(1, "yyyy-MM-dd HH:m:ss"))
  }
  def getDate = new Date()

  def getDiffDayByToday(diff: Int, format: String):String = {
    val ca = Calendar.getInstance()
    ca.add(Calendar.DAY_OF_MONTH, diff)
    val time = ca.getTime
    val sdf = new SimpleDateFormat(format)
    sdf.format(time)
  }
}
