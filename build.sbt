name := "SparkXgboost"

version := "0.1"

scalaVersion := "2.11.6"

// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.3.1"
// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "2.3.1"
// https://mvnrepository.com/artifact/org.apache.spark/spark-mllib
libraryDependencies += "org.apache.spark" % "spark-mllib_2.11" % "2.3.1"
// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.3.1"
// https://mvnrepository.com/artifact/ml.dmlc/xgboost4j-spark
libraryDependencies += "ml.dmlc" % "xgboost4j-spark" % "0.80"
// https://mvnrepository.com/artifact/org.apache.spark/spark-sql-kafka-0-10
libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % "2.3.1"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.0.0"