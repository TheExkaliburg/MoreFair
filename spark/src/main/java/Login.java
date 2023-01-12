import java.util.Arrays;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Calendar;
import java.util.Date;

public class Login {
    public static void main(String[] args) {
        System.out.println("Hello, World!"); // Display the string.
        Arrays.stream(args).forEach(System.out::println);
        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("MongoSparkConnectorIntro")
                .config("spark.mongodb.read.connection.uri", "mongodb://localhost/MreFair")
                .config("spark.mongodb.write.connection.uri", "mongodb://localhost/MoreFair")
                .getOrCreate();

        Dataset<Row> loginRows = MongoConnector.read(spark, "login");

        Dataset<Row> result = loginRows.groupBy("account._id").count().sort("count");

        result.show();

        result.write().format("mongodb").mode("append").save();
    }
}
