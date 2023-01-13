import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

public class SparkUtils {
  public static SparkSession createSparkSession(String[] args) throws Exception {
    if (args.length < 1) {
      throw new IllegalArgumentException("The arguments are meant to be: '{activeProfile}'");
    }

    String database = translateProfileToDatabase(args[0]);

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

  public static String translateProfileToDatabase(String activeProfile) {
    String result = "MoreFair";
    if(activeProfile.equals("staging")) {
      result += "Staging";
    }
    return result;
  }
}
