package com.stuben.event.seriallizer;

import com.alibaba.fastjson.JSON;
import com.stuben.event.protocol.BaseEvent;

public class FastJsonSerializer implements ISerializer {

    @Override public byte[] encode(BaseEvent event) {
        return JSON.toJSONBytes(event);
    }

    @Override public <T> T decode(byte[] data, Class<T> targetClass) {
        return JSON.parseObject(data, targetClass);
    }
}
