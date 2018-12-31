package com.taf.auto.jfx.app.ui;

import com.taf.auto.jira.ITTeams;
import com.taf.auto.json.SparseJsonPojo;
import com.taf.auto.rest.UserPass;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.taf.auto.io.JSONUtil.decode;
import static com.taf.auto.io.JSONUtil.encode;

/**
 * Model for shuttling and persisting settings.
 *
 */
public final class UISettings extends SparseJsonPojo {
    private static final Logger LOG = LoggerFactory.getLogger(UISettings.class);

    public static final String UNDEFINED = "";

    @JsonProperty
    public String username = UNDEFINED;

    /** The password, which is never serialized due to ignore annotation */
    @JsonIgnore
    public String password = UNDEFINED;

    @JsonProperty
    public String projectKey = "";

    @JsonProperty
    @Deprecated
    public int itTeam = ITTeams.NO_TEAM;

    /** Path to the last selected feature file */
    @JsonProperty
    public String originalFeatureFile = UNDEFINED;

    @JsonProperty
    public String lastFeatureDir = UNDEFINED;

    @JsonProperty
    private Map<Class<?>, Object> store = new HashMap<>();

    /**
     * Constructs a credential object from the contained username and password.
     *
     * @return the creds
     * @throws RuntimeException if either username or password is not defined per {@link #isDefined(String)}
     */
    public UserPass peekCreds() {
        if(!isDefined(username))
            throw new RuntimeException("username is not defined");
        if(!isDefined(password))
            throw new RuntimeException("password is not defined");
        return new UserPass(username, password);
    }

    public static boolean isDefined(String setting) {
        return setting != null && UNDEFINED != setting;
    }

    public <O> O peek(Class<O> clazz) {
        try {
            /** The decoded data may not match the desired type */
            Object data = store.get(clazz);
            if(null == data) return clazz.newInstance();
            /** Encode it back to a String to let the proper decode occur */
            byte[] encoded = encode(data);
            return decode(encoded, clazz);
        } catch (Exception e) {
            LOG.error("Failed to decode storage for: " + clazz);
            try {
                return clazz.newInstance();
            } catch (Exception e1) {
                throw new RuntimeException("Failed to instantiate: " + clazz, e1);
            }
        }
    }

    public void poke(Object object) {
        store.put(object.getClass(), object);
    }
}
