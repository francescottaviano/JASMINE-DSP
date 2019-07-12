package model.transformations;

import serde.JsonSerdable;
import utils.Mergeable;
import utils.Tuplable;

import java.io.Serializable;
import java.util.Objects;

public class Tuple2<K, V extends Comparable> implements JsonSerdable, Mergeable<Tuple2<K,V>>, Tuplable<Tuple2<K,V>>, Serializable {

    private K key;
    private V value;

    public Tuple2() { }

    public Tuple2(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(key, tuple2.key);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Tuple2<K,V> other) {
        if (this == other) return 0;
        if (other == null) return 1;
        int res = this.value.compareTo(other.value);
        return res;
    }

    @Override
    public boolean sameKey(Tuple2<K,V> tuple2) {
        if (this == tuple2) return true;
        else return this.key.equals(tuple2.key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Mergeable merge(Mergeable mergeable) {
        if (this.getClass() == mergeable.getClass()) {
            Class valueClass = this.value.getClass();
            Class otherValueClass = ((Tuple2) mergeable).value.getClass();
            if (valueClass == otherValueClass) {
                if (valueClass == Long.class) {
                    Long sum = Long.sum((Long) this.value, (Long) ((Tuple2) mergeable).value);
                    this.setValue((V) sum);
                }
                else if (valueClass == Double.class) {
                    Double sum = Double.sum((Double) this.value, (Double) ((Tuple2) mergeable).value);
                    this.setValue((V) sum);
                }
                else if (valueClass == Mergeable.class) {
                    ((Mergeable) this.value).merge((Mergeable) ((Tuple2) mergeable).value);
                }
            }
        }
        return this;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
