import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class RoundStatistics {
  public static void main(String[] args) throws Exception{




    SparkSession spark = SparkUtils.createSparkSession(args);

    SqlConnector sqlConnector = new SqlConnector(spark);

    Dataset<Row> accounts = sqlConnector.read("account");
    Dataset<Row> game = sqlConnector.read("game");

    accounts.printSchema();

    Dataset<Row> specificAccount = sqlConnector.read("account", "id = 4");

    specificAccount.show();
  }
}
