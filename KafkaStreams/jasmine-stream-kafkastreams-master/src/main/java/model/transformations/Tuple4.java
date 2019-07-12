package model.transformations;

import serde.JsonSerdable;

import java.io.Serializable;

public class Tuple4<K, V1 extends Comparable, V2 extends Comparable, V3 extends Comparable>
        implements JsonSerdable, Comparable<Tuple4<K,V1,V2,V3>>, Serializable {

    private K key;
    private V1 value1;
    private V2 value2;
    private V3 value3;

    private int[] compareOrder;

    public Tuple4() {
        this.init(null, null, null, null, new int[]{1,2,3});
    }

    public Tuple4(K key, V1 value1, V2 value2, V3 value3) {
        this.init(key, value1, value2, value3, new int[]{1,2,3});
    }

    public Tuple4(K key, V1 value1, V2 value2, V3 value3, int[] compareOrder) {
        this.init(key, value1, value2, value3, compareOrder);
    }

    private void init(K key, V1 value1, V2 value2, V3 value3, int[] compareOrder) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
        this.value3 = value3;
        this.compareOrder = compareOrder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Tuple4<K, V1, V2, V3> other) {
        int result = 0;
        for (int i = 0; i < 3; i++) {
            int valueNum = this.compareOrder[i];
            if (valueNum == 1) {
                result = other.getValue1().compareTo(this.value1);
            } else if (valueNum == 2) {
                result = other.getValue2().compareTo(this.value2);
            } else if (valueNum == 3) {
                result = other.getValue3().compareTo(this.value3);
            }
            if (result != 0) {
                return result;
            }
        }
        return result;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V1 getValue1() {
        return value1;
    }

    public void setValue1(V1 value1) {
        this.value1 = value1;
    }

    public V2 getValue2() {
        return value2;
    }

    public void setValue2(V2 value2) {
        this.value2 = value2;
    }

    public V3 getValue3() {
        return value3;
    }

    public void setValue3(V3 value3) {
        this.value3 = value3;
    }
}
