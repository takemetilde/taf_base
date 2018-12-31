package com.taf.auto.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class JSONTraverse {
    private static final Logger LOG = LoggerFactory.getLogger(JSONTraverse.class);

    public static void traverse(String json, Consumer<JsonValue> logic) throws IOException {
        traverse(JSONCompare.getJsonFromResponse(json), logic);
    }

    public static void traverse(JsonStructure structure, Consumer<JsonValue> logic) throws IOException {
        if(structure instanceof JsonArray) {
            JsonArray array = (JsonArray) structure;
            for (int i = 0, len = array.size(); i < len; i++) {
                JsonValue value = array.get(i);
                if(value instanceof JsonStructure) {
                    traverse((JsonStructure) value, logic);
                } else {
                    logic.accept(value);
                }
            }
            return;
        }

        JsonObject jo = (JsonObject) structure;
        for(String key : jo.keySet()) {
            JsonValue value = jo.get(key);
            if(value instanceof JsonStructure) {
                traverse((JsonStructure) value, logic);
            } else {
                logic.accept(value);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String filename = "...\\cucumber.json"; // todo add actual file to complete testing
        JsonStructure structure = JSONCompare.getJsonFromResponse(Paths.get(filename));
        traverse(structure, JSONTraverse::process);
    }

    private static void process(JsonValue value) {
        switch(value.getValueType()) {
            case STRING: String v = ((JsonString)value).getString();
                            if(v.length() > 255) {
                                LOG.warn("Value is longer than 255 (Actual: {}):\n{}", v.length(), value);
                            }
        }
    }
}
