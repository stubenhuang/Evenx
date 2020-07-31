package com.stuben.evenx.seriallizer;

import com.alibaba.fastjson.JSON;
import com.stuben.evenx.protocol.BaseEvenx;

public class FastJsonSerializer implements ISerializer {

    @Override public byte[] encode(BaseEvenx evenx) {
        return JSON.toJSONBytes(evenx);
    }

    @Override public <T> T decode(byte[] data, Class<T> targetClass) {
        return JSON.parseObject(data, targetClass);
    }
}
