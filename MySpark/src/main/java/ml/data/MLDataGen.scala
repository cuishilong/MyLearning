package ml.data

import java.util.Random

import org.apache.spark.sql.{SaveMode, SparkSession}
import util.BeanUtil.UserInfoBean
import util.{DateUtil, PropUtil, ScalaUtil}

object MLDataGen {


  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master(PropUtil.spark_master_local)
      .getOrCreate()

    val arr = new scala.collection.mutable.ArrayBuffer[UserInfoBean]()

    val random = new Random()
    val nameSeed = Map[Int, String]((0, "Arica"), (1, "Lily"), (2, "Hanlei"), (3, "Lucy"), (4, "Lina"), (5, "Anna"), (6, "Nancy"), (7, "Tom"), (8, "Trump"), (9, "cuishilong"))
    val someWord = Map[Int, String]((0, "Hello Word"), (1, "Welcome to China"), (2, "yes"), (3, "I am cuishilong"), (4, "Who are you"), (5, "no"), (6, "I am fine"), (7, "Good luck"), (8, "Sunny day"), (9, "haha"))

    for (i <- 0.to(100 * 10000)) {
      val id = i
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

      arr += bean
    }

    spark
      .createDataFrame(arr)
      .toDF()
      .coalesce(1)
      .write
      .mode(SaveMode.Overwrite)
      .json(ScalaUtil.getDesktopDir + "/data/user_info.csv")

    spark.stop()
  }
}
