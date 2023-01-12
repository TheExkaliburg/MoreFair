import java.util.Arrays;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Calendar;
import java.util.Date;

public class Login {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new Exception("The arguments are meant to be: '{activeProfile}'");
        }

        String db = MongoConnector.translateProfileToDatabase(args[0]);

        SparkSession spark = SparkSession.builder()
                .master("local")
                .appName("MongoSparkConnectorIntro")
                .config("spark.mongodb.read.connection.uri", "mongodb://localhost/" + db)
                .config("spark.mongodb.write.connection.uri", "mongodb://localhost/" + db)
                .getOrCreate();

        Dataset<Row> loginRows = MongoConnector.read(spark, "login");

        Dataset<Row> result = loginRows.groupBy("account._id").count().sort("count");

        result.show();

        result.write().format("mongodb").mode("append").save();
    }
}
