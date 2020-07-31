package com.stuben.evenx.consumer;

import com.alibaba.fastjson.JSON;
import com.stuben.evenx.evenx.RemoteEvenx;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@EvenxConsumerController("local")
public class RemoteConsumerAction {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @EvenxConsumerMapping("com.stuben.evenx.evenx.RemoteEvenx")
    public void consume(String json) {
        logger.info("## " + json);

        RemoteEvenx remoteEvenx = JSON.parseObject(json, RemoteEvenx.class);
        logger.info("## time:" + DateFormatUtils.format(new Date(remoteEvenx.getTimestamp()), "yyyy-MM-dd HH:mm:ss"));
    }
}
