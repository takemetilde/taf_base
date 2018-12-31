package com.taf.auto.io;

import com.taf.auto.common.PrettyPrinter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static com.taf.auto.common.PrettyPrinter.prettyList;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

/**
 * Utilities for dealing with JSON.
 *
 */
public class JSONUtil {
    private static final Logger LOG = LoggerFactory.getLogger(JSONUtil.class);

    private static ObjectMapper mapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    /**
     * Decodes an Object from the given byte array using {@link ObjectMapper#readValue(byte[], Class)}.
     *
     * @param data the raw bytes of the Object to decode
     * @param valueType the Class of the decoded Object
     * @param <T> the type of the decoded Object
     * @return the decoded Object
     * @throws IOException if unable to decode
     */
    public static <T> T decode(byte[] data, Class<T> valueType) throws IOException {
        T object = mapper().readValue(data, valueType);
        return object;
    }

    public static <T> T decode(File file, Class<T> valueType) throws IOException {
        T object = mapper().readValue(file, valueType);
        return object;
    }

    public static <T> T decode(Path file, Class<T> valueType) throws IOException {
        T object = mapper().readValue(file.toFile(), valueType);
        return object;
    }

    /**
     * Encodes the given object to a byte array using {@link ObjectMapper#writeValueAsBytes(Object)}.
     *
     * @param o the Object to encode
     * @return the raw bytes of the JSON string representing the Object
     * @throws JsonProcessingException if unable to encode
     */
    public static byte[] encode(Object o) throws JsonProcessingException {
        return mapper().writeValueAsBytes(o);
    }

    /**
     * Variant of {@link #encode(Object)} that utilizes
     * {@link ObjectMapper#writerWithDefaultPrettyPrinter()}.
     *
     * @param o the Object to encode
     * @return the raw bytes of the JSON string representing the Object
     * @throws JsonProcessingException if unable to encode
     */
    public static byte[] encodePretty(Object o) throws JsonProcessingException {
        return prettyWriter().writeValueAsBytes(o);
    }

    /**
     * Encodes the given object with {@link ObjectMapper#writeValue(File, Object)}.
     *
     * @param file destination of the output
     * @param o the object to encode
     * @throws IOException if unable to encode
     */
    public static void encode(File file, Object o) throws IOException {
        mapper().writeValue(file, o);
    }

    /**
     * Variant of {@link #encode(File, Object)} that utilizes
     * {@link ObjectMapper#writerWithDefaultPrettyPrinter()}.
     *
     * @param file destination of the output
     * @param o the object to encode
     * @throws IOException if unable to encode
     */
    public static void encodePretty(File file, Object o) throws IOException {
        prettyWriter().writeValue(file, o);
    }

    /**
     * Encodes the given object with {@link ObjectMapper#writeValue(File, Object)}.
     *
     * @param file destination of the output
     * @param o the object to encode
     * @throws IOException if unable to encode
     */
    public static void encode(Path file, Object o) throws IOException {
        mapper().writeValue(file.toFile(), o);
    }

    /**
     * Variant of {@link #encode(Path, Object)} that utilizes
     * {@link ObjectMapper#writerWithDefaultPrettyPrinter()}.
     *
     * @param file destination of the output
     * @param o the object to encode
     * @throws IOException in case of an IO issue
     */
    public static void encodePretty(Path file, Object o) throws IOException {
        prettyWriter().writeValue(file.toFile(), o);
    }

    /**
     * Calls {@link #encode(Object)} followed by {@link #decode(byte[], Class)} to create a deep clone of {@code o}.
     *
     * @param o the object to clone
     * @param <O> the type of o
     * @return a deep clone of o
     * @throws IOException if cloning fails
     */
    public static <O> O deepClone(O o) throws IOException {
        byte[] bytes = encode(o);
        return (O)decode(bytes, o.getClass());
    }

    public static String flattenJson(Object o) throws JsonProcessingException {
        return mapper().writeValueAsString(o);
    }

    private static ObjectWriter prettyWriter() {
        return mapper().writerWithDefaultPrettyPrinter();
    }

    public static String prettyPrint(Object o) throws JsonProcessingException {
        try {
            return prettyWriter().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            try {
                Method method = o.getClass().getMethod("prettyPrint");
                return (String) method.invoke(o);
            } catch (NoSuchMethodException nsme) {
                LOG.trace("No prettyPrint method");
            } catch (Exception e2) {
                LOG.error("Failed to invoke prettyPrint", e2);
            }
            throw e;
        }
    }

    public static String pair(Object key, String value) {
        return format("{\"%s\": %s}", key, value);
    }

    public static String pair(Object key, List<String> values) {
        return format("{\"%s\": [%s]}", key, PrettyPrinter.prettyCollection(values, ","));
    }

    public static String pair2(Object key, String value) {
        return pair(key, format("\"%s\"", value));
    }

    public static String namePair2(String value) {
        return pair2("name", value);
    }

    public static String pairs2(List<Pair<String, String>> vals) {
        return "{ " + prettyList(vals.stream()
                .map(p -> format("\"%s\": \"%s\"", p.getKey(), p.getValue()))
                .collect(toList())) + " }";
    }

    /**
     * Quotes the given input text contents using JSON standard quoting.
     *
     * @param input the raw input to quote
     * @return the quoted form
     */
    public static String quote(String input) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(input));
    }
}