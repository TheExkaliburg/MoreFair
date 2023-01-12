import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Calendar;
import java.util.Date;

public class Login {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("MongoSparkConnectorIntro")
                .config("spark.mongodb.read.connection.uri", "mongodb://localhost/moreFair")
                .config("spark.mongodb.write.connection.uri", "mongodb://localhost/moreFair")
                .getOrCreate();

        Dataset<Row> loginRows = MongoConnector.read(spark, "login");

        Dataset<Row> result = loginRows.groupBy("account._id").count().sort("count");

        result.show();

        result.write().format("mongodb").mode("append").save();
    }
}
