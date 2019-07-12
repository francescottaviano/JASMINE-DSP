package operators;

import model.transformations.Tuple2;
import org.apache.kafka.streams.kstream.Aggregator;

public class DecimalCounterAggregator<K> implements Aggregator<K, Double, Tuple2<K, Double>> {

    @Override
    public Tuple2<K, Double> apply(K key, Double value, Tuple2<K, Double> aggregator) {
        aggregator.setKey(key);
        incrementValue(aggregator, value);
        return aggregator;
    }

    private void incrementValue(Tuple2<K,Double> aggregator, Double value) {
        if (aggregator.getValue() == null) {
            aggregator.setValue(value);
        } else {
            aggregator.setValue(aggregator.getValue() + value);
        }
    }
}
