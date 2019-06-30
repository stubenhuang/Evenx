package com.stuben.event.protocol;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class BaseEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1588971947922671499L;

    private String uuid = UUID.randomUUID().toString();

    private BaseEvent(Object source) {
        super(source);
    }

    public BaseEvent() {
        this("");
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
