package streams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import model.output.CommentHourlyCount;
import model.input.CommentInfo;
import model.input.CommentType;
import model.transformations.Tuple2;
import operators.CollectorAggregator;
import operators.CounterAggregator;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import serde.GenericSerde;
import utils.DateUtils;
import utils.SerdeUtils;

import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

public class CommentsCountQuery implements BaseStream {

    private ObjectMapper objectMapper;

    public CommentsCountQuery() {
        this.objectMapper = new ObjectMapper();
    }


    /** ----------------------------------------------------------------------------
     *                                  PROPERTIES
     *  ----------------------------------------------------------------------------
     */
    @Override
    public Properties addProperties(Properties properties) {
        return properties;
    }

    /** ----------------------------------------------------------------------------
     *                                  TOPOLOGY
     *  ----------------------------------------------------------------------------
     */
    @Override
    public void addTopology(KStream<Integer, CommentInfo> baseKStream) {

        /* First window KTable [24h] */
        KTable<Long, HashMap<Integer, Long>> firstWindowKTable = baseKStream
                .filter((key, value) -> value.getCommentType() == CommentType.COMMENT)
                .map((key, value) -> {
                    Calendar calendar = DateUtils.parseCalendar(value.getCreateDate());
                    return KeyValue.pair(
                            (int) Math.floor(calendar.get(Calendar.HOUR_OF_DAY) / 2.0),
                            1L
                    );
                })
                .groupByKey(Grouped.with("comments-count-on-hour", Serdes.Integer(), Serdes.Long()))
                .windowedBy(TimeWindows.of(Duration.ofHours(24)).grace(Duration.ofSeconds(0)))
                .aggregate(
                        Tuple2::new,
                        new CounterAggregator<>(),
                        Materialized.with(Serdes.Integer(), new GenericSerde<>())
                )
                .toStream()
                .map((key, value) -> KeyValue.pair(key.window().start(), value))
                .groupByKey(Grouped.with("hourly-comments-aggregate", Serdes.Long(), new GenericSerde<>()))
                .aggregate(
                        HashMap::new,
                        new CollectorAggregator<>(),
                        Materialized.with(
                                Serdes.Long(),
                                new GenericSerde<>()
                        )
                );

        /* First window results (KStream) [24h] */
        KStream<String, CommentHourlyCount> firstWindowKStream = firstWindowKTable
                .toStream()
                .map((key, value) -> KeyValue.pair(
                        "Comments Count 24 Hours",
                        new CommentHourlyCount(key,
                                value.getOrDefault(0, 0L),
                                value.getOrDefault(1, 0L),
                                value.getOrDefault(2, 0L),
                                value.getOrDefault(3, 0L),
                                value.getOrDefault(4, 0L),
                                value.getOrDefault(5, 0L),
                                value.getOrDefault(6, 0L),
                                value.getOrDefault(7, 0L),
                                value.getOrDefault(8, 0L),
                                value.getOrDefault(9, 0L),
                                value.getOrDefault(10, 0L),
                                value.getOrDefault(11, 0L))
                ));

        /* Second window results (KStream) [7d] */
        KStream<String, CommentHourlyCount> secondWindowKStream = firstWindowKStream
                .groupByKey(Grouped.with("7-days-comments-count-reduce", Serdes.String(), new GenericSerde<>()))
                .windowedBy(TimeWindows.of(Duration.ofDays(7)).grace(Duration.ofSeconds(0)))
                .reduce((toReduce, reducer) -> (CommentHourlyCount) reducer.merge(toReduce))
                .toStream()
                .map((key, value) -> KeyValue.pair(
                        "Comments Count 7 Days",
                        value
                ));

        /* Third window results (KStream) [1M] */
        KStream<String, CommentHourlyCount> thirddWindowKStream = firstWindowKStream
                .groupByKey(Grouped.with("1-month-comments-count-reduce", Serdes.String(), new GenericSerde<>()))
                .windowedBy(TimeWindows.of(Duration.ofDays(30)).grace(Duration.ofSeconds(0)))
                .reduce((toReduce, reducer) -> (CommentHourlyCount) reducer.merge(toReduce))
                .toStream()
                .map((key, value) -> KeyValue.pair(
                        "Comments Count 30 Days",
                        value
                ));

        /* OUTPUT */

        firstWindowKStream.to(
                String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW),
                Produced.with(Serdes.String(), new GenericSerde<>())
        );
        firstWindowKStream.to(
                String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.LARGE_WINDOW),
                Produced.with(Serdes.String(), new GenericSerde<>())
        );
        firstWindowKStream.to(
                String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW),
                Produced.with(Serdes.String(), new GenericSerde<>())
        );

        /*firstWindowKStream.foreach((key, value) ->
                System.out.println(String.format("--------------------- %s ------------------\n" +
                                "%s\n---------------------------------",
        secondWindowKStream.foreach((key, value) ->
                System.out.println(String.format("--------------------- %s ------------------\n" +
                                "%s\n---------------------------------",
                        key, value.toJsonString(objectMapper))));
        thirddWindowKStream.foreach((key, value) ->
                System.out.println(String.format("--------------------- %s ------------------\n" +
                                "%s\n---------------------------------",
                        key, value.toJsonString(objectMapper))));*/

    }
}
