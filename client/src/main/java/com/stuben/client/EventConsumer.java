package com.stuben.client;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class EventConsumer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    public EventConsumer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void init() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(EventController.class);

        if (controllers.isEmpty()) {
            logger.warn("controller is empty");
            return;
        }

        for (String controllerName : controllers.keySet()) {
            logger.debug("init controller : {}  now ", controllerName);



        }

    }

}
