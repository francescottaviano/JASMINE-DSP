package utils;

import java.io.Serializable;

public interface Mergeable<T> extends Serializable, Comparable<T> {
    Mergeable merge(Mergeable mergeable);
}
