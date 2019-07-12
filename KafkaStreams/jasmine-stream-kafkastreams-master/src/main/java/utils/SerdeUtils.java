package utils;


import com.fasterxml.jackson.core.type.TypeReference;
import serde.JsonSerdable;
import serde.JsonSerde;

public class SerdeUtils {

    /* avoid init */
    private SerdeUtils() {}


    public static <T extends JsonSerdable> JsonSerde<T> jsonSerde(TypeReference<T> typeReference) {
        return jsonSerde(typeReference, false);
    }

    public static <T extends JsonSerdable> JsonSerde<T> jsonSerde(TypeReference<T> typeReference, boolean discardMalformedJson) {
        return new JsonSerde<>(typeReference, discardMalformedJson);
    }

}
