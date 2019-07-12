package streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import model.input.CommentInfo;
import model.output.TopArticlesOutput;
import model.transformations.Tuple2;
import operators.CounterAggregator;
import operators.TopAggregateFunction;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import serde.GenericSerde;
import utils.SerializableComparator;
import utils.TopN;

import java.time.Duration;
import java.util.*;
import java.util.function.ToLongFunction;

public class TopArticlesStream implements BaseStream {

    private ObjectMapper objectMapper;

    private Tuple2<String, Duration>[] windows;

    public TopArticlesStream(Tuple2<String, Duration>[] windows) {
        this.windows = windows;
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
    @SuppressWarnings("unchecked")
    @Override
    public void addTopology(KStream<Integer, CommentInfo> baseKStream) {

        /* first window aggregate (KTable) [1h] */
        KTable<Windowed<String>, Tuple2<String,Long>> intermediateFirstWindowKTable = baseKStream
                .map((key, value) -> KeyValue.pair(value.getArticleID(), 1L))
                .groupByKey(Grouped.with("article-id" + this.windows[0].getKey(), Serdes.String(), Serdes.Long()))
                .windowedBy(TimeWindows.of(this.windows[0].getValue()).grace(Duration.ofSeconds(0)))
                .aggregate(
                        () -> new Tuple2<>("", 0L),
                        new CounterAggregator<>(),
                        Materialized.with(Serdes.String(), new GenericSerde<>()));

        /* first window partial and final chart (KTable) [1h] */
        KTable<Long, TopN<Tuple2<String, Long>>> firstWindowChart = intermediateFirstWindowKTable
                .toStream()
                .map((key, value) -> KeyValue.pair(new Tuple2<>(key.window().start(),(int) (Math.random() * 4)), value))
                .groupByKey(Grouped.with(
                        "article-partial-chart-group" + this.windows[0].getKey(),
                        new GenericSerde<>(),
                        new GenericSerde<>()))
                .aggregate(() -> new TopN<>(3, new SerializableComparator<>(true)),
                        new TopAggregateFunction<>(),
                        Materialized.
                                <Tuple2<Long, Integer>, TopN<Tuple2<String, Long>>, KeyValueStore<Bytes, byte[]>>as("article-partial-chart-"  + this.windows[0].getKey() + "-table")
                                .withKeySerde(new GenericSerde<>()).withValueSerde(new GenericSerde<>())
                )
                .toStream()
                .map((key, value) -> KeyValue.pair(key.getKey(), value))
                .groupByKey(Grouped.with(
                        "article-final-chart-" + this.windows[0].getKey(),
                        Serdes.Long(),
                        new GenericSerde<>()))
                .reduce((toReduce, reducer) -> {
                    reducer.groupCharts(toReduce);
                    return reducer;
                });

        /* second window partial and final chart (KTable) [24h] */
        KTable<Long, TopN<Tuple2<String, Long>>> secondWindowChart = firstWindowChart
                .toStream()
                .map((key, value) -> KeyValue.pair(this.windows[0].getKey() + String.valueOf((int) (Math.random() * 4)), new Tuple2<>(key, value)))
                .groupByKey(Grouped.with(
                        "article-partial-chart-group" + this.windows[1].getKey(),
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
                        "article-final-chart-" + this.windows[1].getKey(),
                        Serdes.Long(),
                        new GenericSerde<>()))
                .reduce((toReduce, reducer) -> {
                    reducer.margeRankings(toReduce);
                    return reducer;
                });

        /* third window partial and final chart (KTable) [7d] */
        KTable<Long, TopN<Tuple2<String, Long>>> thirdWindowChart = secondWindowChart
                .toStream()
                .map((key, value) -> KeyValue.pair(this.windows[1].getKey() + String.valueOf((int) (Math.random() * 4)), new Tuple2<>(key, value)))
                .groupByKey(Grouped.with(
                        "article-partial-chart-group" + this.windows[2].getKey(),
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
                        "article-final-chart-" + this.windows[2].getKey(),
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
                    List<Tuple2<String, Long>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top Articles %s -> [ from -> %s ]", this.windows[0].getKey(), new Date(key).toString()),
                            new TopArticlesOutput(key, queueContent)
                    );
                })
                .to(
                    String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.SMALL_WINDOW),
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
                    List<Tuple2<String, Long>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top Articles %s -> [ from -> %s ]", this.windows[1].getKey(), new Date(key).toString()),
                            new TopArticlesOutput(key, queueContent)
                    );
                })
                .to(
                    String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW),
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
                    List<Tuple2<String, Long>> queueContent = value.getTopN();
                    return KeyValue.pair(
                            String.format("Top Articles %s -> [ from -> %s ]", this.windows[2].getKey(), new Date(key).toString()),
                            new TopArticlesOutput(key, queueContent)
                    );
                })
                .to(
                    String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.LARGE_WINDOW),
                    Produced.with(Serdes.String(), new GenericSerde<>())
                );
                /*.foreach((key, value) ->
                        System.out.println(String.format("--------------------- %s ------------------\n" +
                                        "%s\n---------------------------------",
                                key, value.toJsonString(objectMapper))));*/
    }
}
