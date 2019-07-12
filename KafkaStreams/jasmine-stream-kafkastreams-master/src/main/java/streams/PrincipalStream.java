package streams;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;

import java.util.Properties;

public interface PrincipalStream {

    public Properties createProperties(Properties properties, BaseStream... streams);

    public Topology createTopology(StreamsBuilder streamsBuilder, BaseStream... streams);

    public void start(Topology topology, Properties properties);
}
