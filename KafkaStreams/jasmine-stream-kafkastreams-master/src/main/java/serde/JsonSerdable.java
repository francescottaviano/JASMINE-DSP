package serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public interface JsonSerdable {

    default byte[] serialize(ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(this);
    }

    static <T extends JsonSerdable> T deserialize(ObjectMapper objectMapper, byte[] bytes, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(bytes, typeReference);
    }

    default <T extends JsonSerdable> String toJsonString(Class<T> tClass, ObjectMapper objectMapper) {
        try {
            return (tClass == null ? objectMapper.writer() : objectMapper.writerWithView(tClass)).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Cannot serialize object";
        }
    }

    default <T extends JsonSerdable> String toJsonString(ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Cannot serialize object";
        }
    }
}
