package config;

/**
 * config.Configuration Class
 * Edit this class to change system configurations
 * */
public class Configuration {

    public enum DspEngine {
        FLINK, KAFKA_STREAMS
    }

    public static final DspEngine DSP_ENGINE = DspEngine.FLINK;
    /* Kafka Hostname */
    //private static final String KAFKA_HOSTNAME = "www.kafka.simonemancini.eu";
    private static final String KAFKA_HOSTNAME = "localhost";

    /* Kafka Brokers */
    private static final String KAFKA_BROKER_0 = String.format("%s:%d", KAFKA_HOSTNAME, 9092);
    /*private static final String KAFKA_BROKER_0 = String.format("%s:%d", KAFKA_HOSTNAME, 32093);
    private static final String KAFKA_BROKER_1 = String.format("%s:%d", KAFKA_HOSTNAME, 32094);
    private static final String KAFKA_BROKER_2 = String.format("%s:%d", KAFKA_HOSTNAME, 32095);*/

    /* Kafka Input Topics */
    public static final String KAFKA_INPUT_TOPIC = "jasmine-input-topic";

    /* Kafka Output Topics*/
    public static final String BASE_OUTPUT_TOPIC = "jasmine-%s%s-output-topic";
    public static final String KAFKA_TOP_ARTICLES_OUTPUT_TOPIC = "topArticles";
    public static final String KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC = "commentsCount";
    public static final String KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC = "topUserRatings";

    /* Window Size */
    public static final String SMALL_WINDOW = "1h";
    public static final String MEDIUM_WINDOW = "24h";
    public static final String LARGE_WINDOW = "7d";
    public static final String VERY_LARGE_WINDOW = "1M";

    /* Kafka topic default partition number */
    public static final int KAFKA_TOPIC_PARTITIONS = 4;

    /* Kafka topic replication factor */
    public static final Object KAFKA_REPLICATION_FACTOR = 1;

    /* Kafka Consumer Group ID */
    public final static String CONSUMER_GROUP_ID = "consumers-group";

    /* Bootstrap servers */
    public static final String BOOTSTRAP_SERVERS = String.format("%s",KAFKA_BROKER_0);
    //public static final String BOOTSTRAP_SERVERS = String.format("%s,%s,%s",KAFKA_BROKER_0, KAFKA_BROKER_1, KAFKA_BROKER_2);

    /* Application ID */
    public static final String APPLICATION_ID = "JASMINE-comments-analyzer";

    /* Redis */
    public static final String REDIS_HOSTNAME = "localhost";
    public static final int REDIS_PORT = 6379;
    /*public static final String REDIS_HOSTNAME = "www.kafka.simonemancini.eu";
    public static final int REDIS_PORT = 32079;*/


    /* output consumer connfig */
    public static final String OUTPUT_CONSUMER_GROUP_ID = "output-consumer-group";


}
