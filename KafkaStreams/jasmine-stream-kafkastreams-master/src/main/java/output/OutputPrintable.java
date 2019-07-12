package output;

import com.fasterxml.jackson.databind.ObjectMapper;
import serde.JsonSerdable;

import java.io.IOException;
import java.io.Serializable;

public interface OutputPrintable extends Serializable {
    String prettyPrint();

    static <T> T deserializeJson(ObjectMapper objectMapper, String json, Class<T> tClass) throws IOException {
        return objectMapper.readValue(json, tClass);
    }
}
