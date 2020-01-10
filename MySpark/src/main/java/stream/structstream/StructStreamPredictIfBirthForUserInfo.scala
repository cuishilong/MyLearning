package stream.structstream

import com.alibaba.fastjson.{JSON, JSONObject}
import ml.model.MLPredictIfBirthByBinomialLogisticRegressionModel.featureAgeEngineerByFeatureHasher
import org.apache.spark.ml.classification.LogisticRegressionModel
import org.apache.spark.ml.feature._
import org.apache.spark.sql.streaming.{OutputMode, Trigger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import util.BeanUtil

object StructStreamPredictIfBirthForUserInfo {
  val model_load_path = s"hdfs://localhost:9000/model/MLPredictIfBirthByBinomialLogisticRegressionModel"
  val stream_tag = getClass.getSimpleName.replaceAll("\\$", "")
  val master = "local[*]"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName(stream_tag)
      .master(master)
      .getOrCreate()
    import spark.implicits._
    //    spark.streams.addListener(new MyStreamQueryListener(stream_tag))

    val lr_model = LogisticRegressionModel.load(s"${model_load_path}/lr_model")
    val idf_model = IDFModel.load(s"${model_load_path}/idf_model")
    val one_hot_encoder_model = OneHotEncoderModel.load(s"${model_load_path}/one_hot_encoder_model")

    val df = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      // 订阅主题
      .option("subscribe", "user_info_topic")
      .load()
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)", "offset", "timestamp")
      .as[(String, String, Long, Long)]
      .map(e => {
        val jsonObj = JSON.parseObject(e._2)
        val id = jsonObj.getInteger("id")
        val label = jsonObj.getInteger("label")
        val name = jsonObj.getString("name")
        val age = jsonObj.getInteger("age")
        val degree = jsonObj.getInteger("degree")
        val gender = jsonObj.getInteger("gender")
        val declare = jsonObj.getString("declare")
        val dayOfWeek = jsonObj.getInteger("dayOfWeek")
        val dayOfMonth = jsonObj.getInteger("dayOfMonth")
        BeanUtil.UserInfoBean(id, label, name, age, gender, degree, declare, dayOfWeek, dayOfMonth)
      })
      .toDF()

    val query = df
      .writeStream
      .trigger(Trigger.ProcessingTime(1000))
      .outputMode(OutputMode.Append())
      .foreachBatch((ds, batchId) => {
        featuresAssembleAndPredict(ds, lr_model, idf_model, one_hot_encoder_model)
          .select("id", "label", "prediction")
          .show(false)
      })
      .start()

    query.awaitTermination()


    spark.stop()
  }

  def featuresAssembleAndPredict(df: DataFrame, lr_model: LogisticRegressionModel, idf_model: IDFModel, oneHotEncoderModel: OneHotEncoderModel): DataFrame = {
    val declare_features = featureDeclareEngineerByTfidf(df, idf_model)
    val clazz_features = featureGenderEngineerByOneHotEncoder(df, oneHotEncoderModel)
    val age_features = featureAgeEngineerByFeatureHasher(df)
    val features_raw = clazz_features.join(declare_features, "id").join(age_features, "id")
    // 特征组装
    val features = new VectorAssembler()
      .setInputCols(Array("degree_features", "age_features", "gender_features", "declare_features"))
      .setOutputCol("features")
      .transform(features_raw)
    lr_model.transform(features)
  }

  def featureDeclareEngineerByTfidf(df: DataFrame, idf_model: IDFModel): DataFrame = {
    val declares_raw = df.select("id", "declare", "label")
    val tokenizer = new Tokenizer().setInputCol("declare").setOutputCol("declare_tokenizer")
    val declare_tokenizer = tokenizer.transform(declares_raw)
    val declare_tf = new HashingTF()
      .setNumFeatures(4)
      .setInputCol("declare_tokenizer")
      .setOutputCol("declare_tf")
      .transform(declare_tokenizer)
    idf_model.transform(declare_tf)
  }

  def featureGenderEngineerByOneHotEncoder(df: DataFrame, oneHotEncoderModel: OneHotEncoderModel): DataFrame = {
    val clazz_raw = df.select("id", "gender", "degree")
    oneHotEncoderModel.transform(clazz_raw)
  }

  def featureAgeEngineerByBinarizer(df: DataFrame): DataFrame = {
    val age_raw = df.selectExpr("id", "cast(age as double) as age")
    val binarizer = new Binarizer()
      .setInputCol("age")
      .setOutputCol("age_features")
      .setThreshold(30)
    binarizer.transform(age_raw)
  }

  def getStartingOffsets(): String = {
    val jsonObj = new JSONObject()
    val topic = "test"
    val Array(partition, offset) = Array(0, 150000)
    val jsonValue = new JSONObject()
    jsonValue.put(partition.toString, offset)
    jsonObj.put(topic, jsonValue)
    println(jsonObj.toString())
    jsonObj.toString()
  }
}
