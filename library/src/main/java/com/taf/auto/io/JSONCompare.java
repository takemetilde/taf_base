package com.taf.auto.io;

import com.taf.auto.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Comparison between two JSON responses. 
 * Currently used to ensure a Rest API update is a strict enhancement over the previous version.
 */
public class JSONCompare {
    private static final Logger LOG = LoggerFactory.getLogger(JSONCompare.class);
	
//	private static final boolean INFINITE_NESTING = true; // current design does not support setting this to false
//	private static boolean failFast = false; // current design does not support setting this to true;

    /**
	 * Creates a {@link JsonStructure} from the given string representation.
	 *
     * @param json must be formatted like a {@link JsonObject} or a {@link JsonArray}
     * @return associated {@code JsonObject} or {@code JsonArray}
     */
    public static JsonStructure getJsonFromResponse(String json) {
        LOG.debug("Getting JsonObject from: " + json);

        if(null == json || json.isEmpty()) {
            throw new IllegalArgumentException("Valid JSON must be provided");
        }

        InputStream in = null;
        JsonReader reader = null;
        try {
            in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
            reader = Json.createReader(in);
            return reader.read();
        } finally {
            if(null != reader) {
                // this should also close the inputstream
                IOUtil.safeClose(reader);
            } else {
                // failed to instantiate the reader so just close the inpustream
                IOUtil.safeClose(in);
            }
        }
    }

	public static JsonStructure getJsonFromResponse(Path file) throws IOException {
		byte[] bytes = Files.readAllBytes(file);
		return getJsonFromResponse(new String(bytes));
	}

    /**
     * {@code JSON} consists of a top level {@code JsonObject}, which is a comma-delimited list of 
     * <b>key</b> : <b>value</b> pairs, e.g. { "A":"a" , "B":"b" }. 
     * <br>
     * A {@link JsonObject}, <b>S</b>, is a "superset" of another {@code JsonObject} <b>s</b> if, 
     * for every key:value pair in <b>s</b>, there is an identical {@code String} key in <b>S</b> 
     * paired with a value that represents a superset of the value in <b>s</b>.
     * A value can be a {@code String} literal, a nested {@code JsonObject}, or a nested {@code JsonArray}. 
     * <br>
     * A {@link String} value is a "superset" if it is {@code equals} the original value. 
     * <br>
     * A {@link JsonArray} is a simpler version of a {@code JsonObject}; its values do not have keys, e.g. [ "a" , "b" ]. 
     * It follows the same "superset" rules as a {@code JsonObject}.
     * 
     * <p>
     * 
     * The only operations that can be performed on a valid {@code JSON String} to produce a superset are:
     * <ol>
     * <li> Add a new key:value pair to an existing {@code JsonObject}
     * <li> Add a new value to an existing {@code JsonArray}
     * <li> Add a wrapper Object or Array around an value or around several values in an Array
     * </ol>
     * WARNING: wrapping some number of Array values is an inconsistency, and arises from 
     * the problem we currently have with array reductions: [ "A", "A", "A" ] considered subset of [ "A" ]
     * 
     * @param supersetJSON the candidate superset
     * @param subsetJSON the candidate subset
     * @return a list of failure points proving {@code supersetJSON} is not a superset of {@code subsetJSON},
     * or an empty list if the given {@code JSON} strings represent a subset/superset relationship
     */
    public static List<String> supersetComparison(String supersetJSON, String subsetJSON) {
        JsonStructure superset = getJsonFromResponse(supersetJSON);
        JsonStructure subset = getJsonFromResponse(subsetJSON);
        List<String> failures = new ArrayList<>();
		
        failures.addAll(ValueElement.of(subset).isSubsetOf(ValueElement.of(superset)));
        
        return failures;
    }
    
	
	/**
	 * Wrapper for a {@link JsonValue}. Responsible for knowing whether 
	 * another {@code ValueElement} represents a superset of this one.
	 */
	private static class ValueElement {
		
		protected final JsonValue element;
		
		private ValueElement (JsonValue wrapped) {
			if (wrapped == null)
				throw new IllegalArgumentException("JsonValue must not be null");
			this.element = wrapped;
		}
		
		public static ValueElement of(JsonValue json) {
			if (json instanceof JsonObject) {
				return new ObjectElement((JsonObject) json);
			} else if (json instanceof JsonArray) {
				return new ArrayElement((JsonArray) json);
			} else
				return new ValueElement(json);
		}
		
		public static KeyValuePair of(String key, JsonValue json) {
			return new KeyValuePair(key, json);
		}
		
		/** Convenience method for {@link #isSubsetOf(ValueElement, ValueElement)} */
		public final List<String> isSubsetOf(ValueElement other) {
			return isSubsetOf(this, other);
		}

		/**
		 * Fulfills the requirements of {@link JSONCompare#supersetComparison(String, String)} 
		 * 
		 * @param sub candidate subset
		 * @param other candidate superset
		 */
		public static final List<String> isSubsetOf(ValueElement sub, ValueElement other) {
			if (!sub.match(other).isEmpty()) {
				// failed. unwrap the other element and try again on each of its children
				String problems = isSubsetOf(sub, other.contents());
				return problems.isEmpty() ? Collections.emptyList() : Arrays.asList(problems);
			} else {
				// if the element matched, must also check that its contents are represented amongst the other's contents
				return sub.contents()
						  .stream()
						  .map(child -> isSubsetOf(child, other.contents()))
						  .filter(s -> !s.isEmpty())
						  .collect(Collectors.toList());
			}
		}
		
		/**
		 * @return an empty {@code String} if the given {@code ValueElement} passes 
		 * {@link #isSubsetOf(ValueElement, ValueElement)} for any of the other given elements, 
		 * a failure message otherwise 
		 */
		private static final String isSubsetOf(ValueElement one, Collection<ValueElement> others) {
			return others.stream().anyMatch(other -> isSubsetOf(one, other).isEmpty()) 
					? "" : "Failed to match " + one;
		}
		
		/**
		 * Simple, direct match. 
		 * Should <b>not</b> attempt to analyze children of either this element or the given potential match.
		 * 
		 * @param other the {@code ValueElement} to match
		 * @return failure message if it doesn't match, empty {@code String} otherwise
		 */
		protected String match(ValueElement other) {
			return element.equals(other.element) ? "" : "Missing value: " + element.toString();
		}
		
		protected final String messageIfWrongType(ValueElement other) {
			return getClass().isInstance(other) ? "" : 
				getClass().getSimpleName() + " cannot be a subset of " + other.getClass().getSimpleName();
		}
		
		protected Collection<ValueElement> contents() {
			return Collections.emptySet();
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + asString();
		}
		
		/**
		 * @return a {@code String} representation of the {@code JSON} represented by this {@code ValueElement}
		 */
		public String asString() {
			return element.toString();
		}
	}
	
	/** { key : value, key : value, ... } */
	private static class ObjectElement extends ValueElement {
		
		public ObjectElement (JsonObject wrapped) {
			super(wrapped);
		}
		
		@Override
		protected String match(ValueElement other) {
			return messageIfWrongType(other);
		}
		
		@Override
		protected Collection<ValueElement> contents() {
			return ((JsonObject) element).keySet()
										 .stream()
										 .map(key -> of(key, ((JsonObject) element).get(key)))
										 .collect(Collectors.toList());
		}
	}
	
	/** key : value */
	private static class KeyValuePair extends ValueElement {
		
		private final String key;
		
		public KeyValuePair (String key, JsonValue json) {
			super(json);
			this.key = key;
		}
		
		@Override
		protected String match(ValueElement other) {
			if (!(other instanceof KeyValuePair)) 
				return messageIfWrongType(other);
			return key.equals(((KeyValuePair) other).key) ? "" : "Missing key: " + key;
		}
		
		@Override
		protected Collection<ValueElement> contents() {
			return Arrays.asList(of(element));
		}
		
		@Override
		public String asString() {
			return key + ":" + element;
		}
	}

	/** [ value, value, ... ] */
	private static class ArrayElement extends ValueElement {
		
		public ArrayElement (JsonArray wrapped) {
			super(wrapped);
		}
		
		@Override
		protected String match(ValueElement other) {
			return messageIfWrongType(other);
		}
		
		@Override
		protected Collection<ValueElement> contents() {
			return ((JsonArray) element).stream()
										.map(ValueElement::of)
										.collect(Collectors.toList());
		}
	}
}
