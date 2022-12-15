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
                .config("spark.mongodb.read.connection.uri", "mongodb://localhost/test.login")
                .config("spark.mongodb.write.connection.uri", "mongodb://localhost/test.loginResults")
                .getOrCreate();

        Dataset<Row> df = spark.read().format("mongodb").load();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Date yesterday = calendar.getTime();

        // Dataset<Row> entriesFromLastHour = df.where("createdOn >= '" + yesterday + "'");

        Dataset<Row> result = df.groupBy("account._id").count();

        result.show();

        result.write().format("mongodb").mode("append").save();
    }
}
