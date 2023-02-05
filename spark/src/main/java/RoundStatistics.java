import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.first;
import static org.apache.spark.sql.functions.greatest;
import static org.apache.spark.sql.functions.lit;
import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.rank;
import static org.apache.spark.sql.functions.row_number;
import static org.apache.spark.sql.functions.sum;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.functions;

public class RoundStatistics {
  public static void main(String[] args) throws Exception{
    if (args.length < 1) {
      throw new IllegalArgumentException("The arguments are meant to be: '{roundId}'");
    }
    int roundId = Integer.parseInt(args[0]);
    SparkSession spark = SparkUtils.createSparkSession();
    SqlConnector sqlConnector = new SqlConnector(spark);

    /*
    Dataset<Row> round = sqlConnector.query("SELECT * FROM round WHERE id = " + roundId).distinct();
    Dataset<Row> roundType =
        sqlConnector.query("SELECT * from round_type WHERE round_entity_id = " + roundId).distinct();
    Dataset<Row> ladder = sqlConnector.query("SELECT * from ladder WHERE round_id = " + roundId ).distinct();
    Dataset<Row> ladderType =
        sqlConnector.query("SELECT ladder_type.* from ladder_type "
            + "INNER JOIN ladder ON ladder_type.ladder_entity_id = ladder.id "
            + "WHERE ladder.round_id = " + roundId ).distinct();

     */
    Dataset<Row> ranker = sqlConnector.query("SELECT ranker.* from ranker "
        + "INNER JOIN ladder ON ranker.ladder_id = ladder.id "
        + "WHERE ladder.round_id = " + roundId).distinct();
    /*
    Dataset<Row> rankerUnlocks = sqlConnector.query("SELECT ranker_unlocks.* from ranker_unlocks "
        + "INNER JOIN ranker ON ranker_unlocks.id = ranker.id "
        + "INNER JOIN ladder ON ranker.ladder_id = ladder.id "
        + "WHERE ladder.round_id = " + roundId).distinct();

     */

    Dataset<Row> promotedRankers = ranker.filter("growing = false")
        .select("account_id", "ladder_id", "rank");


    WindowSpec window = Window.partitionBy("ladder_id").orderBy(col("rank").desc());
    Dataset<Row> promotedRankerPoints = promotedRankers
        .withColumn("dense_rank",
          functions.dense_rank().over(window))
        .withColumn("promotion_points",
          greatest(expr("10 - dense_rank + 1"), lit(0))
    );


    Dataset<Row> rankerPointsPerLadder = promotedRankerPoints.groupBy("account_id")
        .agg(
            //first("account_id").as("account_id"),
            collect_list("promotion_points").as("points"),
            sum("promotion_points").as("total")
        )
        .filter("total > 0")
        .sort(col("total").desc());

    rankerPointsPerLadder.show(100);
    rankerPointsPerLadder.printSchema();

  }
}
