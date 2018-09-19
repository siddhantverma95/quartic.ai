import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier
import org.apache.spark.SparkConf
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.{IndexToString, StringIndexer, VectorAssembler}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

object SparkPipeline {
  def main(args: Array[String]): Unit = {

    val inputPath = "./src/main/train/train.csv"
    val pipelineOutPath = "./src/main/pipeline"

    //Setting Spark Configuration
    val configuration = new SparkConf()
      .setAppName("Model Training Pipeline")
      .setMaster("local");
    val spark = SparkSession.builder().config(configuration).getOrCreate()

    // Setting up the schema
    val schema = new StructType(Array(
      StructField("Timestamp", StringType, false),
      StructField("FeaA", FloatType, true),
      StructField("FeaB", FloatType, true),
      StructField("FeaC", FloatType, true),
      StructField("FeaD", FloatType, true),
      StructField("FeaE", FloatType, true),
      StructField("Label", FloatType, true)))

    //Loading train.csv file into Spark DataFrame using Schema
    val df = spark
      .read
      .format("csv")
      .option("header", "true")
      .option("mode", "DROPMALFORMED")
      .schema(schema)
      .load(inputPath)

    //Filling null with 0. Not an ideal solution
    val data = df.na.fill(0.0)

    //Converting TimeStamp for training
    val timeIndexer = new StringIndexer()
      .setInputCol("Timestamp")
      .setOutputCol("time")
      .setHandleInvalid("keep")

    //Creating DenseVector for xgboost classifier
    val assembler = new VectorAssembler().
      setInputCols(Array("time","FeaA", "FeaB", "FeaC", "FeaD", "FeaE")).
      setOutputCol("features")

    //Converting timeIndex back to timestamp
    val timeIndexToString = new IndexToString()
      .setInputCol("time")
      .setOutputCol("timeString")

    //Splitting the data into training and test dataset
    val Array(training, test) = data.randomSplit(Array(0.8, 0.2), 123)

    //Defining the Xgboost Params
    val booster = new XGBoostClassifier(
      Map(
        "objective" -> "multi:softprob"
      )
    )
    booster.setLabelCol("Label")
    booster.setFeaturesCol("features")
    booster.setPredictionCol("prediction")
    booster.setSeed(1)
    booster.setNumRound(100)
    booster.setNumClass(5)
    booster.setEvalMetric("merror")

    //Initializing spark pipeline
    val pipeline = new Pipeline()
      .setStages(Array(timeIndexer, assembler, booster, timeIndexToString))
    val model = pipeline.fit(training)

    // Batch prediction
    val prediction = model.transform(test).select("timeString", "prediction","Label")
    prediction.show(50)

    // Model evaluation
    val evaluator = new MulticlassClassificationEvaluator()
    evaluator.setLabelCol("Label")
    val accuracy = evaluator.evaluate(prediction)
    println("The model accuracy is : " + accuracy)

    //Saving up the pipeline model
    model.write.overwrite().save(pipelineOutPath)
  }
}
