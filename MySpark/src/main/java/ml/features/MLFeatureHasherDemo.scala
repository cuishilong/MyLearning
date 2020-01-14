package ml.features

import org.apache.spark.ml.feature.FeatureHasher
import org.apache.spark.sql.SparkSession

/**
 * 使用FeaturesHasher抽取特征（适用于类别特征）
 * 1、直接使用
 * 注：适用于非数值的类别特征
 */
object MLFeatureHasherDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    val dataInit = Seq(
      (true, "0", "foo")
      , (false, "1", "bar")
      , (false, "0", "baz")
      , (false, "1", "foo")
    )

    val data = spark.createDataFrame(dataInit).toDF("is_real", "gender", "tag")

    val feature_hasher = new FeatureHasher()
      .setInputCols("is_real", "gender", "tag")
      .setOutputCol("features")
      .setNumFeatures(12)

    feature_hasher.transform(data).show(false)

    spark.stop()
  }
}
