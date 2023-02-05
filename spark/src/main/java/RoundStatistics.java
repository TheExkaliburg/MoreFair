import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

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


    round.printSchema();
    roundType.printSchema();
    ladder.printSchema();
    ladderType.printSchema();
    ranker.printSchema();
    rankerUnlocks.printSchema();
  }
}
