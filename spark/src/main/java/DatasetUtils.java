import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class DatasetUtils {
  public static Dataset<Row> select(Dataset<Row> dataset, String col, String... cols){
    if(dataset.isEmpty()) {
      try(SparkSession session = dataset.sparkSession()){
        return session.emptyDataFrame();
      }catch (Exception e){
        throw new RuntimeException(e);
      }
    }
    return dataset.select(col, cols);
  }

  public static Dataset<Row> union(Dataset<Row> dataset, Dataset<Row> other) {
    if(other.isEmpty()) {
      return dataset;
    }
    if (dataset.isEmpty()) {
      return other;
    }

    return dataset.union(other);
  }

}
