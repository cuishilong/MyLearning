package ml.classification

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import util.ScalaUtil

object MLBinomialLogisticRegressionDemo {
  val resourcePath = ScalaUtil.getResourceDir
  val desktopPath = "/Users/arica/Desktop"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val features = spark
      .read
      .format("libsvm")
      .load(s"${resourcePath}data/sample_libsvm_data.txt")

    val lr = new LogisticRegression()
      .setMaxIter(10)
      .setRegParam(0.8)
      .setElasticNetParam(0.3)

    val model = lr.fit(features)

    val summary = model.binarySummary

    val accuracy: Double = summary.accuracy
    val roc: DataFrame = summary.roc
    val pr: DataFrame = summary.pr
    val auc: Double = summary.areaUnderROC

    roc.show(500)
    pr.show(500)
    println(s"accuracy : ${accuracy} ; auc : ${auc}")

    pr.coalesce(1).write.mode(SaveMode.Overwrite).csv(desktopPath + "/data/pr")
    roc.coalesce(1).write.mode(SaveMode.Overwrite).csv(desktopPath + "/data/roc")

    spark.stop()
  }
}
