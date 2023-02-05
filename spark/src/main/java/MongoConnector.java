import lombok.Data;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.current_timestamp;
import static org.apache.spark.sql.functions.struct;
import static org.apache.spark.sql.functions.col;


@Data
public class MongoConnector {
  private final SparkSession sparkSession;
  public MongoConnector(SparkSession sparkSession) {
    this.sparkSession = sparkSession;
  }

  public Dataset<Row> read(String collection) {
    // TODO: find a way to only read in the newest record from last Week
    return sparkSession.read().format("mongodb")
        .option("spark.mongodb.read.collection", collection)
        .load();
  }

  public Dataset<Row> read(String collection, String aggregation) {
    return sparkSession.read().format("mongodb")
        .option("spark.mongodb.read.collection", collection)
        .option("spark.mongodb.read.aggregation.pipeline", aggregation)
        .load();
  }

  public void write(Dataset<Row> dataset, String collection) {
   /* Dataset<Row> dataSetWithData = dataset.withColumn("data", struct(col("*")));
    Dataset<Row> dataSetOnlyData = dataSetWithData.groupBy().agg(collect_list("data").alias(
        "data"));
    Dataset<Row> dataSetWithTimestamp = dataSetOnlyData.withColumn("createdOn",
        current_timestamp());

    */

    dataset.printSchema();
    dataset.show();
    dataset.write().format("mongodb").mode("append")
        .option("spark.mongodb.write.collection", collection)
        .save();
  }

}
