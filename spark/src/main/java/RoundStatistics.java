import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.expr;
import static org.apache.spark.sql.functions.row_number;

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

    Dataset<Row> round = sqlConnector.query("SELECT * FROM round WHERE id = " + roundId).distinct();
    Dataset<Row> roundType =
        sqlConnector.query("SELECT * from round_type WHERE round_entity_id = " + roundId).distinct();
    Dataset<Row> ladder = sqlConnector.query("SELECT * from ladder WHERE round_id = " + roundId ).distinct();
    Dataset<Row> ladderType =
        sqlConnector.query("SELECT ladder_type.* from ladder_type "
            + "INNER JOIN ladder ON ladder_type.ladder_entity_id = ladder.id "
            + "WHERE ladder.round_id = " + roundId ).distinct();
    Dataset<Row> ranker = sqlConnector.query("SELECT ranker.* from ranker "
        + "INNER JOIN ladder ON ranker.ladder_id = ladder.id "
        + "WHERE ladder.round_id = " + roundId).distinct();
    Dataset<Row> rankerUnlocks = sqlConnector.query("SELECT ranker_unlocks.* from ranker_unlocks "
        + "INNER JOIN ranker ON ranker_unlocks.id = ranker.id "
        + "INNER JOIN ladder ON ranker.ladder_id = ladder.id "
        + "WHERE ladder.round_id = " + roundId).distinct();

    Dataset<Row> promotedRankers = ranker.filter("growing = false");

    WindowSpec window = Window.partitionBy("ladder_id").orderBy(col("rank").desc());
    Dataset<Row> promotedRankerPoints = promotedRankers.withColumn("dense_rank",
        functions.dense_rank().over(window)
    );

    promotedRankerPoints.show(100);
    promotedRankerPoints.printSchema();

    promotedRankerPoints = promotedRankerPoints.withColumn("promotion_points",
        expr("10 - dense_rank + 1")
    );
    promotedRankerPoints.show(100);
    promotedRankerPoints.printSchema();

    /*
    WindowSpec window = Window.partitionBy("ladder_id").orderBy("rank");
    Dataset<Row> temp = promotedRankers.withColumn("rank", row_number().over(window))
            .filter("rank <= 10")
                .groupBy("ladder_id", "ranker_id").agg(first("rank"))

    groupedByLadder.show(100);

    groupedByLadder = groupedByLadder.withColumn("ladders",
        functions.expr("transform(zip(account_ids, ranks), (r,p) -> p)")
    );

    groupedByLadder.show(100);

    groupedByLadder = groupedByLadder.withColumn("points",
        functions.exp("transform(ladders, (p) -> 11, p)")
    );

    groupedByLadder.show(100);

    round.printSchema();
    roundType.printSchema();
    ladder.printSchema();
    ladderType.printSchema();
    ranker.printSchema();
    rankerUnlocks.printSchema();

     */

    /*
    //Dataset<Row> promotedRankers = ranker.filter("growing = false");
    //Dataset<Row> joined = promotedRankers.join(ladder,
        promotedRankers.col("ladder_id").equalTo(ladder.col("id")));
    //Dataset<Row> sorted = joined.sort(joined.col("position").asc());
    //Dataset<Row> top10 = sorted.groupBy("ladder_id").agg(functions.first("account_id"),
            functions.first("position"))
        .sort("ladder_id", "position").limit(10);
    Dataset<Row> result = top10.withColumn("points", functions.when(top10.col("position").equalTo(1), 10).otherwise(1))
        .groupBy("account_id").agg(functions.sum("points").as("total"));
    Dataset<Row> filtered = result.filter("total > 0");
    Dataset<Row> finalResult = filtered.sort("total").select("account_id", "total");
     */

  }
}
