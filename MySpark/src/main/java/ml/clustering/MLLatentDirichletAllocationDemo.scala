package ml.clustering

import org.apache.spark.ml.clustering.LDA
import org.apache.spark.sql.SparkSession
import util.ScalaUtil

/**
 * 文本主题聚类
 */
object MLLatentDirichletAllocationDemo {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(getClass.getSimpleName.replaceAll("\\$", ""))
      .master("local[*]")
      .getOrCreate()

    // Loads data
    val dataset = spark.read.format("libsvm")
      .load(s"${ScalaUtil.getResourceDir}/data/sample_lda_libsvm_data.txt")

    // Trains a LDA model.
    val lda = new LDA().setK(10).setMaxIter(10)
    val model = lda.fit(dataset)

    val ll = model.logLikelihood(dataset)
    val lp = model.logPerplexity(dataset)
    println(s"The lower bound on the log likelihood of the entire corpus: $ll")
    println(s"The upper bound on perplexity: $lp")

    // Describe topics.
    val topics = model.describeTopics(3)
    println("The topics described by their top-weighted terms:")
    topics.show(false)

    // Shows the result.
    val transformed = model.transform(dataset)
    transformed.show(false)

    spark.stop()
  }
}
