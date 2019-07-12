package streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import model.input.CommentInfo;
import model.output.TopUserRatings;
import model.transformations.Tuple2;
import model.transformations.Tuple3;
import operators.DecimalCounterAggregator;
import operators.TopAggregateFunction;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import redis.clients.jedis.Jedis;
import serde.GenericSerde;
import utils.SerializableComparator;
import utils.TopN;
import utils.redis.RedisClient;

import java.time.Duration;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.ToDoubleFunction;

public class TopUserRatingsStream implements BaseStream {

    private ObjectMapper objectMapper;

    private Tuple2<String, Duration>[] windows;

    private RedisClient redisClient;

    private final double W_A = 0.3;
    private final double W_B = 0.7;

    public TopUserRatingsStream(Tuple2<String, Duration>[] windows, RedisClient redisClient) {
        this.windows = windows;
        this.objectMapper = new ObjectMapper();
        this.redisClient = redisClient;
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
    @SuppressWarnings("unchecked")
    @Override
    public void addTopology(KStream<Integer, CommentInfo> baseKStream) {

        /* likes count (KTable) */
        KTable<Windowed<Tuple2<Long, Long>>, Tuple2<Tuple2<Long, Long>, Double>> likesCountKTable = baseKStream
                .map((key, value) -> KeyValue.pair(
                        new Tuple2<>(value.getUserID(), value.getCommentID()),
                        value.getRecommendations() * (value.isEditorsSelection() ? 1.1 : 1)
                ))
                .groupByKey(Grouped.with("likes-count" + this.windows[0].getKey(), new GenericSerde<>(), Serdes.Double()))
                .windowedBy(TimeWindows.of(this.windows[0].getValue()).grace(Duration.ofSeconds(0)))
                .aggregate(
                        Tuple2::new,
                        new DecimalCounterAggregator<>(),
                        Materialized.
                                <Tuple2<Long, Long>, Tuple2<Tuple2<Long, Long>, Double>, WindowStore<Bytes, byte[]>>as("likes-count-"  + this.windows[0].getKey() + "-table")
                                .withKeySerde(new GenericSerde<>()).withValueSerde(new GenericSerde<>())
                );

        /* indirect comments count (KTable) */
        KTable<Windowed<Long>, Tuple2<Long, Double>> indirectCommentsCountKTable = baseKStream
                .filter((key, value) -> value.getDepth() > 1)
                .map((key, value) -> KeyValue.pair(value.getInReplyTo(), 1.0))
                .groupByKey(Grouped.with("indirect-comments-count" + this.windows[0].getKey(), Serdes.Long(), Serdes.Double()))
                .windowedBy(TimeWindows.of(Duration.ofHours(24)).grace(Duration.ofSeconds(0)))
                .aggregate(
                        Tuple2::new,
                        new DecimalCounterAggregator<>(),
                        Materialized.
                                <Long, Tuple2<Long, Double>, WindowStore<Bytes, byte[]>>as("indirect-comments-count" + this.windows[0].getKey() + "-table")
                                .withKeySerde(Serdes.Long())
                                .withValueSerde(new GenericSerde<>())
                );

        /* likes count (KStream) */
        KStream<Tuple2<Long, Long>, Tuple3<Long, Long, Double>> likesCountKStream = likesCountKTable
               .toStream()
               .map((key, value) -> KeyValue.pair(
                       new Tuple2<>(key.window().start(), value.getKey().getValue()),
                       new Tuple3<>(key.window().start(), value.getKey().getKey(), value.getValue())
               ));

        /* indirect comments count (KStram) */
        KStream<Tuple2<Long, Long>, Tuple3<Long, Long, Double>> indirectCommentsCountKStream = indirectCommentsCountKTable
               .toStream()
               .map((key, value) -> KeyValue.pair(new Tuple2<>(key.window().start(), value.getKey()), new Tuple3<>(key.window().start(), value.getKey(), value.getValue())));

        /* like count - indirect comments count outer join (KStream) */
        KStream<Long, Tuple2<Long, Double>> baseWindowJoinedKStream = likesCountKStream
                .outerJoin(
                        indirectCommentsCountKStream,
                        (likesValue, indirectCommentsValue) -> new Tuple3<>(
                                likesValue != null ? likesValue.getKey() : indirectCommentsValue.getKey(),
                                likesValue != null ? likesValue.getValue1() : retrieveUserID(indirectCommentsValue.getValue1()),
                                (likesValue != null ? W_A * likesValue.getValue2() : 0.0) +
                                        (indirectCommentsValue != null ? W_B * indirectCommentsValue.getValue2() : 0.0)
                        ),
                        JoinWindows.of(this.windows[0].getValue()).grace(Duration.ofSeconds(0)),
                        Joined.with(
                                new GenericSerde<>(), /* key */
                                new GenericSerde<>(),   /* left value */
                                new GenericSerde<>()  /* right value */
                        )
                )
                .filter((key, value) -> value.getValue1() != null)
                .map((key, value) -> KeyValue.pair(value.getKey(), new Tuple2<>(value.getValue1(), value.getValue2())));

        /* first window partial and final chart (KTable) [24h] */
        KTable<Long, TopN<Tuple2<Long, Double>>> firstWindowChart = baseWindowJoinedKStream
                .map((key, value) -> KeyValue.pair(new Tuple2<>(key,(int) (Math.random() * 4)), value))
                .groupByKey(Grouped.with(
                        "top-users-chart-group" + this.windows[0].getKey(),
                        new GenericSerde<>(),
                        new GenericSerde<>()))
                .aggregate(() -> new TopN<>(10, new SerializableComparator<>(true)),
                        new TopAggregateFunction<>(),
                        Materialized.
                                <Tuple2<Long, Integer>, TopN<Tuple2<Long, Double>>, KeyValueStore<Bytes, byte[]>>as("top-users-partial-chart-"  + this.windows[0].getKey() + "-table")
                                .withKeySerde(new GenericSerde<>()).withValueSerde(new GenericSerde<>())
                )
                .toStream()
                .map((key, value) -> KeyValue.pair(key.getKey(), value))
                .groupByKey(Grouped.with(
                        "top-users-chart-" + this.windows[0].getKey(),
                        Serdes.Long(),
                        new GenericSerde<>()))
                .reduce((toReduce, reducer) -> {
                    reducer.groupCharts(toReduce);
                    return reducer;
                });

        /* second window partial and final chart (KTable) [7d] */
        KTable<Long, TopN<Tuple2<Long, Double>>> secondWindowChart = firstWindowChart
                .toStream()
                .map((key, value) -> KeyValue.pair(this.windows[0].getKey() + String.valueOf((int) (Math.random() * 4)), new Tuple2<>(key, value)))
                .groupByKey(Grouped.with(
                        "top-users-chart-group" + this.windows[1].getKey(),
                        Serdes.String(),
                        new GenericSerde<>()))
                .windowedBy(TimeWindows.of(this.windows[1].getValue()).grace(Duration.ofSeconds(0)))
                .reduce((reducer, toReduce) -> {
                    if (reducer.getKey().equals(toReduce.getKey())) {
                        reducer.getValue().groupCharts(toReduce.getValue());
                    } else {
                        reducer.getValue().margeRankings(toReduce.getValue());
                    }
                    return reducer;
                })
                .toStream()
                .map((key, value) -> KeyValue.pair(key.window().start(), value.getValue()))
                .groupByKey(Grouped.with(
                        "top-users-chart-" + this.windows[1].getKey(),
                        Serdes.Long(),
                        new GenericSerde<>()))
                .reduce((toReduce, reducer) -> {
                    reducer.margeRankings(toReduce);
                    return reducer;
                });

        /* third window partial and final chart (KTable) [1M] */
        KTable<Long, TopN<Tuple2<Long, Double>>> thirdWindowChart = secondWindowChart
                .toStream()
                .map((key, value) -> KeyValue.pair(this.windows[1].getKey() + String.valueOf((int) (Math.random() * 4)), new Tuple2<>(key, value)))
                .groupByKey(Grouped.with(
                        "top-users-chart-group" + this.windows[2].getKey(),
                        Serdes.String(),
                        new GenericSerde<>()))
                .windowedBy(TimeWindows.of(this.windows[2].getValue()).grace(Duration.ofSeconds(0)))
                .reduce((reducer, toReduce) -> {
                    if (reducer.getKey().equals(toReduce.getKey())) {
                        reducer.getValue().groupCharts(toReduce.getValue());
                    } else {
                        reducer.getValue().margeRankings(toReduce.getValue());
                    }
                    return reducer;
                })
                .toStream()
                .map((key, value) -> KeyValue.pair(key.window().start(), value.getValue()))
                .groupByKey(Grouped.with(
                        "top-users-chart-" + this.windows[2].getKey(),
                        Serdes.Long(),
                        new GenericSerde<>()))
                .reduce((toReduce, reducer) -> {
                    reducer.margeRankings(toReduce);
                    return reducer;
                });

        /* first window chart */
        firstWindowChart
                .toStream()
                .map((key, value) -> {
                    List<Tuple2<Long, Double>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top User Ratings %s -> [ from -> %s ]", this.windows[0].getKey(), new Date(key).toString()),
                            new TopUserRatings(key, queueContent)
                    );
                })
                .to(
                        String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW),
                        Produced.with(Serdes.String(), new GenericSerde<>())
                );
                /*.foreach((key, value) ->
                        System.out.println(String.format("--------------------- %s ------------------\n" +
                                        "%s\n---------------------------------",
                                key, value.toJsonString(objectMapper))));*/

        /* second window chart */
        secondWindowChart
                .toStream()
                .map((key, value) -> {
                    List<Tuple2<Long, Double>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top User Ratings %s -> [ from -> %s ]", this.windows[1].getKey(), new Date(key).toString()),
                            new TopUserRatings(key, queueContent)
                    );
                })
                .to(
                        String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.LARGE_WINDOW),
                        Produced.with(Serdes.String(), new GenericSerde<>())
                );
                /*.foreach((key, value) ->
                        System.out.println(String.format("--------------------- %s ------------------\n" +
                                        "%s\n---------------------------------",
                                key, value.toJsonString(objectMapper))));*/

        /* third window chart */
        thirdWindowChart
                .toStream()
                .map((key, value) -> {
                    List<Tuple2<Long, Double>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top User Ratings %s -> [ from -> %s ]", this.windows[2].getKey(), new Date(key).toString()),
                            new TopUserRatings(key, queueContent)
                    );
                })
                .to(
                        String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW),
                        Produced.with(Serdes.String(), new GenericSerde<>())
                );
                /*.foreach((key, value) ->
                        System.out.println(String.format("--------------------- %s ------------------\n" +
                                        "%s\n---------------------------------",
                                key, value.toJsonString(objectMapper))));*/
    }

    /**
     *  resolve user id using comment id
     */
    private Long retrieveUserID(Long commentID) {
        Jedis client = redisClient.getClient();
        String userID = client.get(String.valueOf(commentID));
        redisClient.closeClient(client);
        if(userID != null) return Long.parseLong(userID);
        else return null;
    }
}
