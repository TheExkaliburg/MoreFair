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
    if(System.getenv("PROFILE").equals("staging")) {
      result += "Staging";
    }
    return result;
  }
}
