import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.DataFrame

object XgBoostModel {

  val modelPath = "./src/main/pipeline"

  //Loading up the pipeline model for predictions
  val model = PipelineModel.read.load(modelPath)

  def transform(df: DataFrame):Unit = {
    val df_clean = df.na.fill(0.0)

    val results = model.transform(df_clean).select("Timestamp","prediction")
    //For debugging only
    results.show()
  }

}
