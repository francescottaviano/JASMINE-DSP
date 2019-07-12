package output;

import config.Configuration;
import model.output.CommentHourlyCount;
import model.output.TopArticlesOutput;
import model.output.TopUserRatings;

import java.util.ArrayList;
import java.util.List;

public class OutputKafkaConsumer {

    public static void main(String[] args) {

        String consumerGroupId = Configuration.CONSUMER_GROUP_ID;

        List<OutputKafkaConsumerThread> consumers = null;

        if (Configuration.DSP_ENGINE == Configuration.DspEngine.FLINK) {
            consumers = createFlinkConsumers(consumerGroupId);
        } else if (Configuration.DSP_ENGINE == Configuration.DspEngine.KAFKA_STREAMS) {
            consumers = createKafkaStreamsConsumers(consumerGroupId);
        }

        if (consumers != null) {
            for (OutputKafkaConsumerThread c : consumers) {
                Thread t = new Thread(c);
                t.start();
            }
        }

    }

    public static List<OutputKafkaConsumerThread> createFlinkConsumers(String consumerGroupId) {
        List<OutputKafkaConsumerThread> consumers = new ArrayList<>();

        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, String>(consumerGroupId, 0, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.SMALL_WINDOW), TopArticlesOutput.class));
        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, String>(consumerGroupId, 1, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), TopArticlesOutput.class));
        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, String>(consumerGroupId, 2, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), TopArticlesOutput.class));

        //consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, String>(consumerGroupId, 3, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), CommentHourlyCount.class));
        //consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, String>(consumerGroupId, 4, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), CommentHourlyCount.class));
        //consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, String>(consumerGroupId, 5, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW), CommentHourlyCount.class));

        //consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, String>(consumerGroupId, 6, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), TopUserRatings.class));
        //consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, String>(consumerGroupId, 7, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), TopUserRatings.class));
        //consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, String>(consumerGroupId, 8, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW), TopUserRatings.class));

        return consumers;
    }

    public static List<OutputKafkaConsumerThread> createKafkaStreamsConsumers(String consumerGroupId) {
        List<OutputKafkaConsumerThread> consumers = new ArrayList<>();

        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, TopArticlesOutput>(consumerGroupId, 0, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.SMALL_WINDOW), TopArticlesOutput.class));
        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, TopArticlesOutput>(consumerGroupId, 1, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), TopArticlesOutput.class));
        consumers.add(new OutputKafkaConsumerThread<TopArticlesOutput, String, TopArticlesOutput>(consumerGroupId, 2, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_ARTICLES_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), TopArticlesOutput.class));

        consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, CommentHourlyCount>(consumerGroupId, 3, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), CommentHourlyCount.class));
        consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, CommentHourlyCount>(consumerGroupId, 4, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), CommentHourlyCount.class));
        consumers.add(new OutputKafkaConsumerThread<CommentHourlyCount, String, CommentHourlyCount>(consumerGroupId, 5, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_COMMENTS_COUNT_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW), CommentHourlyCount.class));

        consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, TopUserRatings>(consumerGroupId, 6, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.MEDIUM_WINDOW), TopUserRatings.class));
        consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, TopUserRatings>(consumerGroupId, 7, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.LARGE_WINDOW), TopUserRatings.class));
        consumers.add(new OutputKafkaConsumerThread<TopUserRatings, String, TopUserRatings>(consumerGroupId, 8, String.format(Configuration.BASE_OUTPUT_TOPIC, Configuration.KAFKA_TOP_USER_RATINGS_OUTPUT_TOPIC, Configuration.VERY_LARGE_WINDOW), TopUserRatings.class));

        return consumers;
    }
}
