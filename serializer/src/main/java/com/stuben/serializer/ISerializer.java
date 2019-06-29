package com.stuben.serializer;

import com.stuben.event.BaseEvent;

public interface ISerializer {

    byte[] encode(BaseEvent event);

    <T> T decode(byte[] data, Class<T> targetClass);
}
