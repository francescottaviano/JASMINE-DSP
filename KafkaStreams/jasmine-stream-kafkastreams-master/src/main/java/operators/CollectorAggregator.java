package operators;

import model.transformations.Tuple2;
import org.apache.kafka.streams.kstream.Aggregator;

import java.util.HashMap;


public class CollectorAggregator<K, VK, VV extends Comparable> implements Aggregator<K, Tuple2<VK, VV>, HashMap<VK, VV>> {

    @Override
    public HashMap<VK, VV> apply(K key, Tuple2<VK, VV> keyValue, HashMap<VK, VV> aggregator) {
        aggregator.put(keyValue.getKey(), keyValue.getValue());
        return aggregator;
    }
}
