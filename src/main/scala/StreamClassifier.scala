import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object StreamClassifier {

  def main(args: Array[String]): Unit = {

    //Test.csv input path
    val inputPath = "./src/main/test"

    val configuration = new SparkConf()
      .setAppName("StreamClassifier")
      .setMaster("local[*]")
      .set("spark.sql.streaming.unsupportedOperationCheck", "false")
      .set("spark.sql.streaming.checkpointLocation", "./src/main/checkpoint/textStream")

    //Creating Spark Session
    val spark = SparkSession.builder().config(configuration).getOrCreate()

    // Load dataset
    val schema = new StructType(Array(
      StructField("Timestamp", StringType, true),
      StructField("FeaA", FloatType, true),
      StructField("FeaB", FloatType, true),
      StructField("FeaC", FloatType, true),
      StructField("FeaD", FloatType, true),
      StructField("FeaE", FloatType, true)))

    //Converting test.csv into Spark Data Stream
    val df = spark
      .readStream
      .option("header", "true")
      .option("mode", "DROPMALFORMED")
      .schema(schema)
      .csv(inputPath)

    //Feeding the DataStream into our Xgboost model
    val writeStream = df
      .writeStream
      .format("XGBoostSink")
      .option("topic", "timeSeries")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("truncate","false")
      .start()

    df.printSchema()
    writeStream.awaitTermination()

    import spark.implicits._

    //Subscribing to timeSeries Topic
    val df1 = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "timeSeries")
      .load()

    //Converting kafka Stream into Json and then to the defined schema
    val df2 = df1.selectExpr("CAST(value AS STRING)").as[(String)]
      .select(from_json($"value", schema).as("data"))
      .select("data.*")

    //Feeding schema to our Xgboost trained model
    val df3 = df2.writeStream
      .format("XGBoostSink")
      .option("truncate","false")
      .start()

    df2.printSchema()
    df3.awaitTermination()
  }
}