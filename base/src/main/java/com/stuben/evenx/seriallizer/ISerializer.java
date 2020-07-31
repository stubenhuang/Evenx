package com.stuben.evenx.seriallizer;

import com.stuben.evenx.protocol.BaseEvenx;

public interface ISerializer {

    byte[] encode(BaseEvenx evenx);

    <T> T decode(byte[] data, Class<T> targetClass);
}
