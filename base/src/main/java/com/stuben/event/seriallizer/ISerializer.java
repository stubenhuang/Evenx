package com.stuben.event.seriallizer;

import com.stuben.event.protocol.BaseEvent;

public interface ISerializer {

    byte[] encode(BaseEvent event);

    <T> T decode(byte[] data, Class<T> targetClass);
}
