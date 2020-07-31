package com.stuben.evenx.consumer;

import com.stuben.evenx.evenx.LocalEvenx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EvenxConsumerController
public class LocalConsumerAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EvenxConsumerMapping
    public void consume(LocalEvenx localEvenx) {
        logger.info("## " + localEvenx.getContent());
    }
}
