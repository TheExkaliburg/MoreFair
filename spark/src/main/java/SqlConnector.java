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
        .option("url", "jdbc:postgresql://localhost:5432/MoreFair")
        .option("driver", "org.postgresql.Driver")
        .option("user", System.getenv("SQL_USERNAME"))
        .option("password", System.getenv("SQL_PASSWORD"))
        .option("dbtable", table)
        .load();
  }

  public Dataset<Row> query(String query) {
    return sparkSession.read().format("jdbc")
        .option("url", "jdbc:postgresql://localhost:5432/MoreFair")
        .option("driver", "org.postgresql.Driver")
        .option("user", System.getenv("SQL_USERNAME"))
        .option("password", System.getenv("SQL_PASSWORD"))
        .option("query", query)
        .load();
  }
}
