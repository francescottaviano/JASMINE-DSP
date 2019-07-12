package streams;

import model.input.CommentInfo;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;

public interface BaseStream {
    public Properties addProperties(Properties properties);

    public void addTopology(KStream<Integer, CommentInfo> baseKStream);
}
