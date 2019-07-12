package model.transformations;

import serde.JsonSerdable;

import java.io.Serializable;

public class Tuple3<K, V1 extends Comparable, V2 extends Comparable>
        implements JsonSerdable, Comparable<Tuple3<K,V1,V2>>, Serializable {

    private K key;
    private V1 value1;
    private V2 value2;

    private int[] compareOrder;

    public Tuple3() {
        this.init(null, null, null, new int[]{1, 2});
    }

    public Tuple3(K key, V1 value1, V2 value2) {
        this.init(key, value1, value2, new int[]{1, 2});
    }

    public Tuple3(K key, V1 value1, V2 value2, int[] compareOrder) {
        this.init(key, value1, value2, compareOrder);
    }

    private void init(K key, V1 value1, V2 value2, int[] compareOrder) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
        this.compareOrder = compareOrder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(Tuple3<K, V1, V2> other) {
        int result = 0;
        for (int i = 0; i < 2; i++) {
            int valueNum = this.compareOrder[i];
            if (valueNum == 1) {
                result = other.value1.compareTo(this.value1);
            } else if (valueNum == 2) {
                result = other.value2.compareTo(this.value2);
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

    public int[] getCompareOrder() {
        return compareOrder;
    }

    public void setCompareOrder(int[] compareOrder) {
        this.compareOrder = compareOrder;
    }
}