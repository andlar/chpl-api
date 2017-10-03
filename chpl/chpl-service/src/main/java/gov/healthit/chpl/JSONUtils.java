package gov.healthit.chpl;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JSONUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ObjectReader READER = MAPPER.reader();
    private static final ObjectWriter WRITER = MAPPER.writer();

    public static ObjectReader getReader() {
        return READER;
    }

    public static ObjectWriter getWriter() {
        return WRITER;
    }

    public static String toJSON(Object obj) throws JsonProcessingException {

        String json = null;
        if (obj != null) {
            json = getWriter().writeValueAsString(obj);
        }
        return json;
    }

    public static <T> T fromJSON(String json, Class<T> type) throws JsonProcessingException, IOException {

        JsonNode node = getReader().readTree(json);
        T obj = getReader().treeToValue(node, type);
        return obj;

    }

    public static boolean jsonEquals(String json1, String json2) throws JsonProcessingException, IOException {

        Boolean equals;

        try {
            JsonNode node1 = getReader().readTree(json1);
            JsonNode node2 = getReader().readTree(json2);
            equals = node1.equals(node2);

        } catch (final NullPointerException e) {
            equals = false;
        }
        return equals;
    }

}
