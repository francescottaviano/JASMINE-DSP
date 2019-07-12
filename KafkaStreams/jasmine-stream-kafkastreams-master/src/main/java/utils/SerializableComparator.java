package utils;

import java.io.Serializable;
import java.util.Comparator;

public class SerializableComparator<T extends Comparable> implements Comparator<T>, Serializable {

    boolean reversed;

    public SerializableComparator(boolean reversed) {
        this.reversed = reversed;
    }

    @SuppressWarnings("unchecked")
    public int compare (T o1, T o2) {
        if (reversed)
            return o2.compareTo(o1);
        else
            return o1.compareTo(o2);
    }

}
