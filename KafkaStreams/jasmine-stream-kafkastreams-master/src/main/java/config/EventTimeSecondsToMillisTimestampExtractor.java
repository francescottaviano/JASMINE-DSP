package config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.input.CommentInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.errors.StreamsException;
import org.apache.kafka.streams.processor.FailOnInvalidTimestamp;
import org.apache.kafka.streams.processor.TimestampExtractor;

// Extracts the embedded timestamp of a record (giving "event-time" semantics).
public class EventTimeSecondsToMillisTimestampExtractor implements TimestampExtractor {

    @Override
    public long extract(final ConsumerRecord<Object, Object> record, final long previousTimestamp) {
        // get timestamp from comment info record
        long timestamp = -1;
        final CommentInfo commentInfo = (CommentInfo) record.value();
        if (commentInfo != null) {
            timestamp = commentInfo.getCreateDate() * 1000;
        }
        if (timestamp < 0) {
            // Invalid timestamp!  Attempt to estimate a new timestamp,
            // otherwise fall back to record time (event-time or ingestionTime).
            if (previousTimestamp >= 0) {
                return previousTimestamp;
            } else {
                timestamp = record.timestamp();
                return timestamp < 0 ? this.onInvalidTimestamp(record) : timestamp;
            }
        }

        return timestamp;
    }

    private long onInvalidTimestamp(ConsumerRecord<Object, Object> record) {
        String message = null;
        try {
            message = String.format("No timestamp found in record : %s" , new ObjectMapper().writeValueAsString(record));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new StreamsException(message);
    }
}
