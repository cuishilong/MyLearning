package ml.model

import org.apache.spark.ml.classification.{LogisticRegression, LogisticRegressionModel}
import org.apache.spark.ml.feature._
import org.apache.spark.sql.{DataFrame, SparkSession}
import util.{PropUtil, ScalaUtil}

object MLPredictIfBirthByBinomialLogisticRegressionModel {
  val tag = getClass.getSimpleName.replaceAll("\\$", "")
  val model_base_path = s"hdfs://localhost:9000/model/${tag}"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(tag)
      .master(PropUtil.spark_master_local)
      .getOrCreate()

    val df = getDataInit(spark)

    val declare_features = featureDeclareEngineerByTfidf(df)

    val clazz_features = featureGenderEngineerByOneHotEncoder(df)

    val age_features = featureAgeEngineerByFeatureHasher(df)

    val features_raw = clazz_features.join(declare_features, "id").join(age_features, "id")

    val features = new VectorAssembler()
      .setInputCols(Array("degree_features", "declare_features", "age_features", "gender_features"))
      .setOutputCol("features")
      .transform(features_raw)
    features
      .select("id", "label", "degree_features", "declare_features", "age_features", "gender_features")
      .show(false)

    val lr_model = trainLogisticRegressionModel(features)

    //    model.save(ScalaUtil.getDesktopDir + "/data/model/" + tag)
    lr_model.save(s"${model_base_path}/lr_model")

    val predictionDf = lr_model.transform(features.select("id", "label", "features", "age", "age_features"))
    predictionDf.show(false)

    predictionDf.groupBy("prediction").count().show()
    predictionDf.groupBy("label").count().show()

    val summary = lr_model.binarySummary

    println(summary.accuracy)

    spark.stop()
  }

  def trainLogisticRegressionModel(trainData: DataFrame): LogisticRegressionModel = {
    val lr = new LogisticRegression()
    //      .setMaxIter(10)
    //      .setRegParam(0.8)
    //      .setElasticNetParam(1.0)
    lr.fit(trainData)
  }

  def featureDeclareEngineerByWord2Vector(df: DataFrame): DataFrame = {
    val declares_raw = df.select("id", "declare", "label")
    val tokenizer = new Tokenizer().setInputCol("declare").setOutputCol("declare_tokenizer")
    val declare_tokenizer = tokenizer.transform(declares_raw)
    val remover = new StopWordsRemover().setInputCol("declare_tokenizer").setOutputCol("declare_remover")
    val declare_remover = remover.transform(declare_tokenizer)
    val word2vector = new Word2Vec()
      .setInputCol("declare_remover")
      .setOutputCol("declare_features")
      .setVectorSize(4)
      .setMinCount(0)
    val declare_model = word2vector.fit(declare_remover)
    declare_model.transform(declare_remover)
  }

  def featureDeclareEngineerByTfidf(df: DataFrame): DataFrame = {
    val declares_raw = df.select("id", "declare", "label")
    val tokenizer = new Tokenizer().setInputCol("declare").setOutputCol("declare_tokenizer")
    val declare_tokenizer = tokenizer.transform(declares_raw)
    val declare_tf = new HashingTF()
      .setNumFeatures(4)
      .setInputCol("declare_tokenizer")
      .setOutputCol("declare_tf")
      .transform(declare_tokenizer)
    val idf_model = new IDF()
      .setInputCol("declare_tf")
      .setOutputCol("declare_features")
      .fit(declare_tf)
    idf_model.save(s"${model_base_path}/idf_model")
    idf_model.transform(declare_tf)
  }

  def featureGenderEngineerByOneHotEncoder(df: DataFrame): DataFrame = {
    val clazz_raw = df.select("id", "gender", "degree")
    val encoder = new OneHotEncoderEstimator()
      .setInputCols(Array("gender", "degree"))
      .setOutputCols(Array("gender_features", "degree_features"))
    val one_hot_encoder_model = encoder.fit(clazz_raw)
    one_hot_encoder_model.save(s"${model_base_path}/one_hot_encoder_model")
    one_hot_encoder_model.transform(clazz_raw)
  }

  def featureAgeEngineerByFeatureHasher(df: DataFrame): DataFrame = {
    val age_raw = df.select("id", "age")
    val hasher = new FeatureHasher()
      .setNumFeatures(1)
      .setInputCols("age")
      .setOutputCol("age_features")
    hasher.transform(age_raw)
  }

  def featureAgeEngineerByBinarizer(df: DataFrame): DataFrame = {
    val age_raw = df.selectExpr("id", "cast(age as double) as age")
    val binarizer = new Binarizer()
      .setInputCol("age")
      .setOutputCol("age_features")
      .setThreshold(30)
    binarizer.transform(age_raw)
  }

  def getDataInit(spark: SparkSession): DataFrame = {
    spark
      .read
      .json(ScalaUtil.getDesktopDir + "/data/user_info.csv")
  }
}
