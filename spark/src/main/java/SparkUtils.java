import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.struct;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkUtils {

  public static SparkSession createSparkSession() throws Exception {
    String database = getDatabaseName();

    return SparkSession.builder()
        .master("local")
        .appName("MongoSparkConnectorIntro")
        .config("spark.mongodb.read.connection.uri", "mongodb://localhost/")
        .config("spark.mongodb.read.database", database)
        .config("spark.mongodb.write.connection.uri", "mongodb://localhost/")
        .config("spark.mongodb.write.database", database)
        .config("spark.mongodb.write.operationType", "insert")
        .getOrCreate();
  }

  public static String getDatabaseName() {
    String result = "MoreFair";
    if (System.getenv("PROFILE").equals("staging")) {
      result += "Staging";
    }
    return result;
  }

  public static Dataset<Row> addDatasetAsChild(Dataset<Row> parent, Dataset<Row> child,
      String alias) {
    return parent.join(child
        .withColumn(alias, struct(col("*")))
        .groupBy().agg(
            collect_list(alias).alias(alias)
        ));
  }
}
