import java.util.function.Function;
import javax.annotation.Nonnull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@Data
@Slf4j
public class NullableDatasetRow {
  @Nonnull
  private Dataset<Row> dataset;

  private Long count = null;

  public NullableDatasetRow(@Nonnull Dataset<Row> dataset) {
    this.dataset = dataset;
  }

  public NullableDatasetRow select(String col, String... cols) {
    if(this.isEmpty()) {
      return this;
    }

    return apply((dataset) -> dataset.select(col, cols));
  }

  public NullableDatasetRow union(NullableDatasetRow other) {
    if(other.isEmpty()) {
      return this;
    }
    if (this.isEmpty()) {
      return other;
    }

    return apply((dataset) -> dataset.union(other.getDataset()));
  }

  public boolean isEmpty() {
    try {
      return this.count() == 0;
    } catch (Exception e) {
      return true;
    }
  }

  public long count() {
    if(count != null) {
      return this.count;
    }

    try {
      count = dataset.count();
      return count;
    } catch (Exception e) {
      return 0;
    }
  }

  public NullableDatasetRow apply(Function<Dataset<Row>, Dataset<Row>> function) {
    return new NullableDatasetRow(function.apply(this.getDataset()));
  }

}
