import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class Login {
  public static void main(String[] args) {
    SparkSession spark = SparkSession.builder()
        .master("local")
        .appName("MongoSparkConnectorIntro")
        .config("spark.mongodb.read.connection.uri", "mongodb://localhost/test.login")
        .config("spark.mongodb.write.connection.uri", "mongodb://localhost/test.loginResults")
        .getOrCreate();

    Dataset<Row> df = spark.read().format("mongo").load();//.as(Encoders.bean(/*LoginEntity
    // .class*/));
    df.show();
  }
}
