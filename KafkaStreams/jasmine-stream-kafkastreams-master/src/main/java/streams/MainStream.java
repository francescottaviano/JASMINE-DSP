package streams;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import config.EventTimeSecondsToMillisTimestampExtractor;
import model.input.CommentInfo;
import model.transformations.Tuple2;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import redis.clients.jedis.Jedis;
import serde.GenericSerde;
import serde.JsonSerdable;
import utils.SerdeUtils;
import utils.redis.RedisClient;

import java.time.Duration;
import java.util.Properties;

public class MainStream implements PrincipalStream {

    private ObjectMapper objectMapper;

    private RedisClient redisClient;

    public MainStream(RedisClient redisClient) {
        this.objectMapper = new ObjectMapper();
        this.redisClient = redisClient;
    }

    public MainStream() {this.objectMapper = new ObjectMapper();}

    @SuppressWarnings("unchecked")
    public static void main(final String... args) {
        final Properties properties = new Properties();
        final StreamsBuilder streamsBuilder = new StreamsBuilder();

        /* main */
        MainStream mainStream = new MainStream(new RedisClient(Configuration.REDIS_HOSTNAME, Configuration.REDIS_PORT));
        /* top 3 articles */
        TopArticlesStream topArticlesStream = new TopArticlesStream(
                new Tuple2[]{
                        new Tuple2<>(Configuration.SMALL_WINDOW, Duration.ofHours(1)),
                        new Tuple2<>(Configuration.MEDIUM_WINDOW, Duration.ofHours(5)),
                        new Tuple2<>(Configuration.LARGE_WINDOW, Duration.ofDays(7))});
        /* comment count */
        CommentsCountQuery commentsCountQuery = new CommentsCountQuery();
        /* top user ratings */
        TopUserRatingsStream topUserRatingsStream = new TopUserRatingsStream(
                new Tuple2[]{
                        new Tuple2<>(Configuration.MEDIUM_WINDOW, Duration.ofHours(24)),
                        new Tuple2<>(Configuration.LARGE_WINDOW, Duration.ofDays(7)),
                        new Tuple2<>(Configuration.VERY_LARGE_WINDOW, Duration.ofDays(30))},
                mainStream.redisClient);

        /* streams array */
        BaseStream[] baseStreams = new BaseStream[] {topArticlesStream, commentsCountQuery, topUserRatingsStream};

        /* create properties and topology */
        mainStream.createProperties(properties, baseStreams);
        Topology topology = mainStream.createTopology(streamsBuilder, baseStreams);

        /* start */
        mainStream.start(topology, properties);
    }

    /** ----------------------------------------------------------------------------
     *                                  PROPERTIES
     *  ----------------------------------------------------------------------------
     */
    @Override
    public Properties createProperties(Properties properties, BaseStream... streams) {

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, Configuration.APPLICATION_ID);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, Configuration.CONSUMER_GROUP_ID);

        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);

        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, EventTimeSecondsToMillisTimestampExtractor.class);

        properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, Configuration.KAFKA_REPLICATION_FACTOR);

        properties.put(StreamsConfig.WINDOW_STORE_CHANGE_LOG_ADDITIONAL_RETENTION_MS_CONFIG, 10 * 1000);

        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        properties.put(StreamsConfig.RETRIES_CONFIG, 10);
        properties.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG, 30 * 1000);

        //properties.put(StreamsConfig.METRICS_RECORDING_LEVEL_CONFIG, "DEBUG");

        if (streams != null && streams.length > 0) {
            for (BaseStream stream : streams) {
                stream.addProperties(properties);
            }
        }

        return properties;
    }

    /** ----------------------------------------------------------------------------
     *                                  TOPOLOGY
     *  ----------------------------------------------------------------------------
     */
    @Override
    public Topology createTopology(StreamsBuilder streamsBuilder, BaseStream... streams) {

        /* parse comments count and partition trough instances */
        KStream<Integer, CommentInfo> commentInfoKStream = streamsBuilder
                .stream(
                        Configuration.KAFKA_INPUT_TOPIC,
                        Consumed.with(
                                Serdes.String(),
                                SerdeUtils.jsonSerde(new TypeReference<CommentInfo>() {}, true)
                        )
                )
                .filter((key,value) -> value != null)
                .map((key, value) -> KeyValue.pair((int) (Math.random() * Configuration.KAFKA_TOPIC_PARTITIONS), value));

        /* map user id on comment id */
        commentInfoKStream
                .peek((key, value) -> {
                    Jedis client = redisClient.getClient();
                    client.set(String.valueOf(value.getCommentID()), String.valueOf(value.getUserID()));
                    redisClient.closeClient(client);
                });

        /* build queries topology */
        if (streams != null && streams.length > 0) {
            for (BaseStream stream : streams) {
                stream.addTopology(commentInfoKStream);
            }
        }

        return streamsBuilder.build();
    }

    /** ----------------------------------------------------------------------------
     *                                    START
     *  ----------------------------------------------------------------------------
     */
    @Override
    public void start(Topology topology, Properties properties) {
        final KafkaStreams streams = new KafkaStreams(topology, properties);

        //DEBUG
        //System.out.println(topology.describe().toString());

        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
