package com.stuben.evenx.protocol;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class BaseEvenx implements Serializable {

    private String uuid = UUID.randomUUID().toString();
    private long timestamp = System.currentTimeMillis();

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
