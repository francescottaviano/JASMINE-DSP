package model.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.transformations.Tuple2;
import output.OutputPrintable;
import serde.JsonSerdable;

import java.io.Serializable;
import java.util.List;

public class TopArticlesOutput implements JsonSerdable, Serializable, OutputPrintable {

    private long ts;
    private String artID_1;
    private long nCmnt_1;
    private String artID_2;
    private long nCmnt_2;
    private String artID_3;
    private long nCmnt_3;

    public TopArticlesOutput() {
    }

    public TopArticlesOutput(long ts, String artID_1, long nCmnt_1, String artID_2,
                             long nCmnt_2, String artID_3, long nCmnt_3) {
        this.ts = ts;
        this.artID_1 = artID_1;
        this.nCmnt_1 = nCmnt_1;
        this.artID_2 = artID_2;
        this.nCmnt_2 = nCmnt_2;
        this.artID_3 = artID_3;
        this.nCmnt_3 = nCmnt_3;
    }

    public TopArticlesOutput(long ts, List<Tuple2<String, Long>> chart) {
        this(ts,
            chart.size() > 0 ? chart.get(0).getKey() : "",
            chart.size() > 0 ? chart.get(0).getValue() : 0L,
            chart.size() > 1 ? chart.get(1).getKey() : "",
            chart.size() > 1 ? chart.get(1).getValue() : 0L,
            chart.size() > 2 ? chart.get(2).getKey() : "",
            chart.size() > 2 ? chart.get(2).getValue() : 0L);
    }

    @Override
    public String prettyPrint() {
        return this.toJsonString(new ObjectMapper());
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getArtID_1() {
        return artID_1;
    }

    public void setArtID_1(String artID_1) {
        this.artID_1 = artID_1;
    }

    public long getnCmnt_1() {
        return nCmnt_1;
    }

    public void setnCmnt_1(long nCmnt_1) {
        this.nCmnt_1 = nCmnt_1;
    }

    public String getArtID_2() {
        return artID_2;
    }

    public void setArtID_2(String artID_2) {
        this.artID_2 = artID_2;
    }

    public long getnCmnt_2() {
        return nCmnt_2;
    }

    public void setnCmnt_2(long nCmnt_2) {
        this.nCmnt_2 = nCmnt_2;
    }

    public String getArtID_3() {
        return artID_3;
    }

    public void setArtID_3(String artID_3) {
        this.artID_3 = artID_3;
    }

    public long getnCmnt_3() {
        return nCmnt_3;
    }

    public void setnCmnt_3(long nCmnt_3) {
        this.nCmnt_3 = nCmnt_3;
    }
}
