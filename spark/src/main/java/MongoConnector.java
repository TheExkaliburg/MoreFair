import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class MongoConnector {
  public static Dataset<Row> read(SparkSession spark, String collection) {
    return spark.read().format("mongo").option("collection", collection).load();
  }
}
