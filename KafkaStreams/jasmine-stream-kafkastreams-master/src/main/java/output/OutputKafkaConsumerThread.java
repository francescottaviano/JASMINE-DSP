package output;

import com.fasterxml.jackson.databind.ObjectMapper;
import config.Configuration;
import model.transformations.Tuple2;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import serde.GenericDeserializer;
import serde.GenericSerde;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OutputKafkaConsumerThread<T extends OutputPrintable, K extends Serializable, V extends Serializable> implements Runnable {

    private String consumerGroupID;
    private Consumer<K, V> consumer;
    private int id;
    private String topic;
    private Class<T> tClass;
    private Configuration.DspEngine DSP_ENGINE = Configuration.DSP_ENGINE;
    private ObjectMapper objectMapper;
    private OutputPrintableFactory<T> outputPrintableFactory;

    public OutputKafkaConsumerThread(String consumerGroupID, int id, String topic, Class<T> tClass) {
        this.consumerGroupID = consumerGroupID;
        this.id = id;
        this.topic = topic;
        this.tClass = tClass;
        this.objectMapper = new ObjectMapper();
        this.consumer = createConsumer();
        this.outputPrintableFactory = new OutputPrintableFactory<>();

        this.subscribeToTopic();
    }

    private Consumer<K, V> createConsumer() {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Configuration.BOOTSTRAP_SERVERS);

        props.put(ConsumerConfig.GROUP_ID_CONFIG, Configuration.OUTPUT_CONSUMER_GROUP_ID);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new KafkaConsumer<>(props, new GenericDeserializer<>(), new GenericDeserializer<>());
    }

    private void subscribeToTopic(){

        // To consume data, we first need to subscribe to the topics of interest
        consumer.subscribe(Collections.singletonList(this.topic));

    }

    public void save(String key, OutputPrintable output) {
        if (key != null)
            System.out.println(String.format("KEY -> %s", key));
        System.out.println(output.prettyPrint());
    }

    @SuppressWarnings("unchecked")
    public void run() {

        boolean running = true;
        System.out.println("Consumer " + id + " running...");
        try {
            while (running) {
                Thread.sleep(1000);
                ConsumerRecords<K,V> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<K, V> record : records) {

                    if (DSP_ENGINE == Configuration.DspEngine.FLINK) {
                        ConsumerRecord<String, String> flinkRecord = (ConsumerRecord<String, String>) record;
                        OutputPrintable output = this.outputPrintableFactory.createNew(this.tClass, flinkRecord.value(), this.objectMapper);
                        save(flinkRecord.key(), output);
                    } else if (DSP_ENGINE == Configuration.DspEngine.KAFKA_STREAMS) {
                        ConsumerRecord<String, ? extends OutputPrintable> kafkaStreamsRecord = (ConsumerRecord<String, ? extends OutputPrintable>) record;
                        save(kafkaStreamsRecord.key(), kafkaStreamsRecord.value());
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }

    }
}
