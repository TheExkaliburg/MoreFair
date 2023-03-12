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
  public final static long RECENT_TIMESTAMP = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L;
  public final static String RECENT_PIPELINE = "[{'$match': {'createdOn': {'$gte': new Date(" + RECENT_TIMESTAMP + ")}}}]";
  private final SparkSession sparkSession;
  public MongoConnector(SparkSession sparkSession) {
    this.sparkSession = sparkSession;
  }

  public Dataset<Row> read(String collection) {
    return sparkSession.read().format("mongodb")
        .option("spark.mongodb.read.collection", collection)
        .load();
  }

  public Dataset<Row> readRecent(String collection) {
    // TODO: find a way to only read in the newest record from last Week
    System.out.println(RECENT_TIMESTAMP);
    System.out.println(RECENT_PIPELINE);
    return read(collection, RECENT_PIPELINE);
  }

  public Dataset<Row> read(String collection, String aggregationPipeline) {
    return sparkSession.read().format("mongodb")
        .option("spark.mongodb.read.collection", collection)
        .option("spark.mongodb.read.aggregation.pipeline", aggregationPipeline)
        .load();
  }

  public void write(Dataset<Row> dataset, String collection) {
    dataset.printSchema();
    dataset.show();
    dataset.write().format("mongodb").mode("append")
        .option("spark.mongodb.write.collection", collection)
        .save();
  }

}
