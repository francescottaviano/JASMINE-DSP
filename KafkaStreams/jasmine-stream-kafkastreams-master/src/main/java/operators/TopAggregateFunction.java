package operators;

import org.apache.kafka.streams.kstream.Aggregator;
import utils.Mergeable;
import utils.TopN;

public class TopAggregateFunction<K, T extends Mergeable> implements Aggregator<K, T, TopN<T>> {

    @Override
    public TopN<T> apply(K s, T t, TopN<T> topN) {
        topN.offer(t, false);
        return topN;
    }
}