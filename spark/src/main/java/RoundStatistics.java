import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class RoundStatistics {
  public static void main(String[] args) throws Exception{
    /*
    SparkSession spark = SparkUtils.createSparkSession(args);

    Dataset<Row> biasRows = MongoConnector.read(spark, "bias");
    Dataset<Row> multiRows = MongoConnector.read(spark, "multi");
    Dataset<Row> promoteRows = MongoConnector.read(spark, "promote");
    Dataset<Row> autoPromoteRows = MongoConnector.read(spark, "autoPromote");
    Dataset<Row> throwVinegarRows = MongoConnector.read(spark, "throwVinegar");

    Dataset<Row> actionByAccount = biasRows.select("ranker.account", "createdOn")
        .union(multiRows.select("ranker.account", "createdOn"))
        .union(promoteRows.select("ranker.account", "createdOn"))
        .union(autoPromoteRows.select("ranker.account", "createdOn"))
        .union(throwVinegarRows.select("ranker.account", "createdOn"))
        .withColumnRenamed("ranker.account", "account");
  */
  }
}
