package utils;

import serde.JsonSerdable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TopN<T extends Mergeable> implements JsonSerdable, Serializable, Comparable {

    private Comparator<T> comparator;
    private List<T> ranking;
    private int topN;

    public TopN(int n, SerializableComparator<T> comparator) {
        this.comparator = comparator;
        this.ranking = new ArrayList<>();
        this.topN = n;
    }

    public boolean offer(T t, boolean merge) {

        int oldSize = ranking.size();
        int oldPosition = findIndex(t);


        int newPosition = -1;
        if (oldPosition != -1) {
            if (merge) {
                T newT = merge(ranking, oldPosition, t); /* merge, remove and return */
                newPosition = add(newT);
            }
            else {
                ranking.remove(t);
                newPosition = add(t);
            }
        } else {
            newPosition = add(t);
        }

        int newSize = ranking.size();

        if (newPosition > -1) {
            if (newPosition == oldPosition &&
                    oldSize == newSize) {
                return false;
            } else if (newPosition > topN - 1) {
                return false;
            }
        } else {
            return false;
        }

        return true;

    }

    private T merge(List<T> ranking, int oldPosition, T t) {
        T oldT = ranking.remove(oldPosition);
        t.merge(oldT);
        return t;
    }

    public TopN margeRankings(TopN<T> other) {
        List<T> otherRank = other.ranking;
        for (int i = 0; i <  otherRank.size(); i++) {
            this.offer(otherRank.get(i), true);
        }
        return this;
    }

    public TopN groupCharts(TopN<T> other) {
        List<T> otherRank = other.ranking;
        for (int i = 0; i <  otherRank.size(); i++) {
            this.offer(otherRank.get(i), false);
        }
        return this;
    }

    private int add(T t) {

        int insertionPoint = Collections.binarySearch(ranking, t, comparator);
        ranking.add((insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1, t);
        insertionPoint = (insertionPoint > -1) ? insertionPoint : (-insertionPoint) - 1;
        return insertionPoint;

    }

    private int findIndex(T t) {

        for (int i = 0; i < ranking.size(); i++) {
            if (t.equals(ranking.get(i)))
                return i;
        }

        return -1;

    }

    public List<T> getTopN(){

        List<T> top = new ArrayList<>();

        if (ranking.isEmpty())
            return top;

        int elems = Math.min(topN, ranking.size());

        for (int i = 0; i < elems; i++){
            top.add(ranking.get(i));
        }

        return top;

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
