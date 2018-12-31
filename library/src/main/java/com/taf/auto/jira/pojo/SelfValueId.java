package com.taf.auto.jira.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by AF04261 on 12/20/2016.
 */
public class SelfValueId extends SelfId {
    @JsonProperty
    public String value;

    public SelfValueId() {
    }

    public SelfValueId(String value) {
        this.value = value;
    }

    public SelfValueId(Number value) {
        this.value = value.toString();
    }

    public static SelfValueId viaId(String id) {
        SelfValueId val = new SelfValueId();
        val.id = id;
        return val;
    }

    public static SelfValueId viaId(Number id) {
        return viaId(id.toString());
    }

    public static SelfValueId[] viaIds(Number... ids) {
        SelfValueId[] svis = new SelfValueId[ids.length];
        for(int i = 0; i < ids.length; i++ ) {
            svis[i] = viaId(ids[i]);
        }
        return svis;
    }
}
