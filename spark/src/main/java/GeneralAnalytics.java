import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.collect_list;
import static org.apache.spark.sql.functions.count;
import static org.apache.spark.sql.functions.current_timestamp;
import static org.apache.spark.sql.functions.desc;
import static org.apache.spark.sql.functions.hour;
import static org.apache.spark.sql.functions.lag;
import static org.apache.spark.sql.functions.lead;
import static org.apache.spark.sql.functions.struct;
import static org.apache.spark.sql.functions.sum;
import static org.apache.spark.sql.functions.timestamp_seconds;
import static org.apache.spark.sql.functions.to_utc_timestamp;
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

        NullableDatasetRow biasRows = new NullableDatasetRow(mongoConnector.read("bias"));
        NullableDatasetRow multiRows =  new NullableDatasetRow(mongoConnector.read("multi"));
        NullableDatasetRow promoteRows =  new NullableDatasetRow(mongoConnector.read("promote"));
        NullableDatasetRow autoPromoteRows =  new NullableDatasetRow(mongoConnector.read("autoPromote"));
        NullableDatasetRow throwVinegarRows =  new NullableDatasetRow(mongoConnector.read("throwVinegar"));

        Dataset<Row> actionByAccount = biasRows.select("ranker.account", "createdOn")
            .union(multiRows.select("ranker.account", "createdOn"))
            .union(promoteRows.select("ranker.account", "createdOn"))
            .union(autoPromoteRows.select("ranker.account", "createdOn"))
            .union(throwVinegarRows.select("ranker.account", "createdOn"))
            .getDataset().withColumnRenamed("ranker.account", "account");

        WindowSpec window = Window.partitionBy("account").orderBy("createdOn");
        Column createdOnColumn = unix_timestamp(col("createdOn"));
        Column nextCreatedOnColumn = unix_timestamp(lead(col("createdOn"), 1).over(window));
        Column differenceInColumns = nextCreatedOnColumn.minus(createdOnColumn);

        Dataset<Row> accountActionsWithDiff = actionByAccount
            .withColumn("diff",
                // When the createdOn are closer than 30 minutes, then the difference is added in
                // the diff column, otherwise we add a minute of activity
                when(differenceInColumns.leq(1800), differenceInColumns).otherwise(60)
        ).withColumn("hour", hour(col("createdOn")));

        Dataset<Row> accountActivityInSeconds = accountActionsWithDiff
            .groupBy("account").sum("diff")
            .withColumnRenamed("sum(diff)", "activityInSec")
            .sort(desc("activityInSec"));

        Dataset<Row> timePerHour = accountActionsWithDiff.groupBy("hour")
            .agg(sum("diff").as("totalSeconds"))
            .orderBy(col("hour"));


        accountActivityInSeconds.printSchema();
        timePerHour.printSchema();

        Dataset<Row> analysis = accountActivityInSeconds
            .withColumn("accountActivity", struct(col("*")));
        analysis = analysis.groupBy()
            .agg(collect_list("accountActivity").alias("accountActivity"));
        analysis = analysis.withColumn("createdOn", current_timestamp());
        //analysis = analysis.withColumn("activityPerHour", struct(timePerHour.col("*")));

        analysis.printSchema();
        analysis.show();
    }

}
