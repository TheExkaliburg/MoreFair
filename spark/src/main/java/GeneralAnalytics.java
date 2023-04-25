import static org.apache.spark.sql.functions.avg;
import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.count_distinct;
import static org.apache.spark.sql.functions.current_timestamp;
import static org.apache.spark.sql.functions.dayofweek;
import static org.apache.spark.sql.functions.hour;
import static org.apache.spark.sql.functions.lead;
import static org.apache.spark.sql.functions.struct;
import static org.apache.spark.sql.functions.sum;
import static org.apache.spark.sql.functions.to_date;
import static org.apache.spark.sql.functions.unix_timestamp;
import static org.apache.spark.sql.functions.when;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

@Slf4j
public class GeneralAnalytics {

  public static void main(String[] args) throws Exception {
    SparkSession spark = SparkUtils.createSparkSession();

    // Dataset<Row> loginRows = MongoConnector.read(spark, "login");
    // Dataset<Row> result = loginRows.groupBy("account._id").count().sort("count");
    // MongoConnector.write(result, "loginCount");

    MongoConnector mongoConnector = new MongoConnector(spark);
    SqlConnector sqlConnector = new SqlConnector(spark);

    /*
    NullableDatasetRow biasRows = new NullableDatasetRow(mongoConnector.readRecent("bias"));
    NullableDatasetRow multiRows = new NullableDatasetRow(mongoConnector.readRecent("multi"));
    NullableDatasetRow promoteRows = new NullableDatasetRow(mongoConnector.readRecent("promote"));
    NullableDatasetRow autoPromoteRows = new NullableDatasetRow(
        mongoConnector.readRecent("autoPromote"));
    NullableDatasetRow throwVinegarRows = new NullableDatasetRow(
        mongoConnector.readRecent("throwVinegar"));
     */

    Dataset<Row> biasRows = mongoConnector.readRecent("bias");
    Dataset<Row> multiRows = mongoConnector.readRecent("multi");
    Dataset<Row> promoteRows = mongoConnector.readRecent("promote");
    Dataset<Row> autoPromoteRows = mongoConnector.readRecent("autoPromote");
    Dataset<Row> throwVinegarRows = mongoConnector.readRecent("throwVinegar");
    Dataset<Row> messagesSent = sqlConnector.query("SELECT message.* FROM message "
            + "WHERE created_on >= NOW() - INTERVAL '28 days'")
        .withColumnRenamed("created_on", "createdOn")
        .withColumnRenamed("account_id", "account");

    Dataset<Row> actionByAccount = biasRows.select("ranker.account", "createdOn")
        .union(multiRows.select("ranker.account", "createdOn"))
        .union(promoteRows.select("ranker.account", "createdOn"))
        .union(autoPromoteRows.select("ranker.account", "createdOn"))
        .union(throwVinegarRows.select("ranker.account", "createdOn"))
        //.getDataset()
        .withColumnRenamed("ranker.account", "account")
        .union(messagesSent.select("account", "createdOn"));

    WindowSpec window = Window.partitionBy("account").orderBy("createdOn");
    Column createdOnColumn = unix_timestamp(col("createdOn"));
    Column nextCreatedOnColumn = unix_timestamp(lead(col("createdOn"), 1).over(window));
    Column differenceInColumns = nextCreatedOnColumn.minus(createdOnColumn);

    Dataset<Row> accountActionsWithDiff = actionByAccount
        .withColumn("diff",
            // When the createdOn are closer than 30 minutes, then the difference is added in
            // the diff column, otherwise we add a minute of activity
            when(differenceInColumns.leq(1800), differenceInColumns).otherwise(60)
        )
        .withColumn("hour", hour(col("createdOn")))
        .withColumn("weekday", dayofweek(col("createdOn")))
        .withColumn("date", to_date(col("createdOn")));

    // Using activity times to get total/avg times per account/hour/weekday/date
    Dataset<Row> timePerAccount = accountActionsWithDiff.groupBy("account", "date")
        .agg(sum("diff").as("totalSeconds"))
        .groupBy("account").agg(
            avg("totalSeconds").as("avgSeconds"),
            sum("totalSeconds").as("totalSeconds"),
            count_distinct(col("date")).as("days")
        )
        .orderBy(col("account"));

    Dataset<Row> timePerHour = accountActionsWithDiff.groupBy("hour", "date")
        .agg(sum("diff").as("totalSeconds"))
        .groupBy("hour").agg(
            avg("totalSeconds").as("avgSeconds"),
            sum("totalSeconds").as("totalSeconds")
        )
        .orderBy(col("hour"));

    Dataset<Row> timePerWeekday = accountActionsWithDiff.groupBy("weekday", "date")
        .agg(sum("diff").as("totalSeconds"))
        .groupBy("weekday").agg(
            avg("totalSeconds").as("avgSeconds"),
            sum("totalSeconds").as("totalSeconds"))
        .orderBy(col("weekday"));

    Dataset<Row> timePerDay = accountActionsWithDiff.groupBy("date")
        .agg(sum("diff").as("totalSeconds"))
        .orderBy("date");

    // Writing everything into a final object
    Dataset<Row> activityAnalytics = timePerAccount
        .withColumn("timePerAccount", struct(col("*")))
        .groupBy().agg(
            current_timestamp().alias("createdOn"),
            collect_list("timePerAccount").alias("timePerAccount")
        );
    activityAnalytics = SparkUtils.addDatasetAsChild(activityAnalytics, timePerHour, "timePerHour");
    activityAnalytics = SparkUtils.addDatasetAsChild(activityAnalytics, timePerWeekday,
        "timePerWeekday");
    activityAnalytics = SparkUtils.addDatasetAsChild(activityAnalytics, timePerDay, "timePerDay");

    mongoConnector.write(activityAnalytics, "generalAnalysis");
  }

}
