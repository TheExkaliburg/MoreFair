import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class MongoConnector {
  public static Dataset<Row> read(SparkSession spark, String collection) {
    return spark.read().format("mongodb").option("spark.mongodb.read.collection", collection).load();
  }

  public static String translateProfileToDatabase(String activeProfile) {
    String result = "MoreFair";
    if(activeProfile.equals("staging")) {
      result += "Staging";
    }
    return result;
  }

}
