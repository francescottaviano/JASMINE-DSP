package output;

import com.fasterxml.jackson.databind.ObjectMapper;
import serde.GenericDeserializer;

import java.io.IOException;

public class OutputPrintableFactory<T extends OutputPrintable> {

    public OutputPrintableFactory(){}

    public OutputPrintable createNew(Class<T> tClass, String stringJson, ObjectMapper objectMapper) throws IOException {
        return T.deserializeJson(objectMapper, stringJson, tClass);
    }

    public OutputPrintable createNew(byte[] bytes, GenericDeserializer<T> deserializer) {
        return deserializer.deserialize(bytes);
    }
}
