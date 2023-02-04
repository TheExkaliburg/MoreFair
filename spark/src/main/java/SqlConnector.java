import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SqlConnector {

  private final SparkSession sparkSession;

  public SqlConnector(SparkSession sparkSession) {
    this.sparkSession = sparkSession;
  }

  public Dataset<Row> read(String table) {
    return sparkSession.read().format("jdbc")
        .option("url", "jdbc:postgresql://localhost:5432/MoreFairStaging")
        .option("dbtable", table)
        .option("user", "postgresql")
        .option("password", "...")
        .load();
  }
}
