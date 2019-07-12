package operators;

import model.transformations.Tuple2;
import org.apache.kafka.streams.kstream.Aggregator;

public class CounterAggregator<K> implements Aggregator<K, Long, Tuple2<K, Long>> {

    @Override
    public Tuple2<K, Long> apply(K key, Long value, Tuple2<K, Long> aggregator) {
        aggregator.setKey(key);
        incrementValue(aggregator, value);
        return aggregator;
    }

    private void incrementValue(Tuple2<K,Long> aggregator, Long value) {
        if (aggregator.getValue() == null) {
            aggregator.setValue(value);
        } else {
            aggregator.setValue(aggregator.getValue() + value);
        }
    }
}
