package com.stuben.client;

import com.stuben.event.protocol.BaseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

public interface IEventHandler<T extends BaseEvent> extends ApplicationListener<T> {
    Logger logger = LoggerFactory.getLogger(IEventHandler.class);

    @Override
    default void onApplicationEvent(T event) {
        try {
            handle(event);
        } catch (Throwable e) {
            handleException(e);
        }
    }

    void handle(T event);

    /**
     * 处理异常
     *
     * @param exception
     */
    default void handleException(Throwable exception) {
        logger.error("IEventHandler , error", exception);
    }
}
