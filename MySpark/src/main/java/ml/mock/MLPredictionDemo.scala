package ml.mock

import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.linalg.Vectors
import org.apache.spark.sql.SparkSession
import util.ScalaUtil

object MLPredictionDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .getOrCreate()

    val model = LogisticRegressionModel.load(s"${ScalaUtil.getDesktopDir}/data/model/MLPredictBirthByBinomialLogisticRegression")
    val prediction = model.predict(Vectors.sparse(10, Seq((0, 1.0))))
    println(prediction)

    spark.stop()
  }
}
