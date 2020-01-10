package util

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

object DateUtil {
  def main(args: Array[String]): Unit = {
    println(ScalaUtil.getResourceDir)
  }

  def getDate = new Date()

  def getDiffDayByToday(diff: Int, format: String): String = {
    val ca = Calendar.getInstance()
    ca.add(Calendar.DAY_OF_MONTH, diff)
    val time = ca.getTime
    val sdf = new SimpleDateFormat(format)
    sdf.format(time)
  }

  def getDiffDayByToday(diff: Int): Date = {
    val ca = Calendar.getInstance()
    ca.add(Calendar.DAY_OF_MONTH, diff)
    ca.getTime
  }

  def getDayOfMonth(diff: Int): Int = {
    val diffDate = getDiffDayByToday(diff)
    val ca = Calendar.getInstance()
    ca.setTime(diffDate)
    ca.get(Calendar.DAY_OF_MONTH)
  }

  def getDayOfWeek(diff: Int): Int = {
    val diffDate = getDiffDayByToday(diff)
    val ca = Calendar.getInstance()
    ca.setTime(diffDate)
    ca.get(Calendar.DAY_OF_WEEK)
  }
}
