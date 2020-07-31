package com.stuben.evenx.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.stuben.evenx.protocol.BaseEvenx;

public class EvenxInvoker {
    private final static Logger LOGGER = LoggerFactory.getLogger(EvenxInvoker.class);

    private Object controller;
    private Method method;
    private Class<?> paramType;

    public static EvenxInvoker init(Object controller, Method method, Class<?> paramType) {
        return new EvenxInvoker(controller, method, paramType);
    }

    private EvenxInvoker(Object controller, Method method, Class<?> paramType) {
        this.controller = controller;
        this.method = method;
        this.paramType = paramType;
    }

    public void invoke(byte[] datas) {
        try {
            if (paramType.equals(String.class)) {
                invokeMethod(new String(datas));
            } else {
                invokeMethod(JSON.parseObject(datas, paramType));
            }
        } catch (Exception e) {
            LOGGER.warn("invoke error, datas:{}", new String(datas));
        }
    }

    public void invoke(BaseEvenx baseEvenx) {
        try {
            if (paramType.equals(String.class)) {
                invokeMethod(JSON.toJSONString(baseEvenx));
            } else {
                invokeMethod(baseEvenx);
            }
        } catch (Exception e) {
            LOGGER.warn("invoke error, datas:{}", baseEvenx.toString());
        }
    }

    private void invokeMethod(Object object) {
        try {
            if (null == method) {
                return;
            }

            method.invoke(controller, object);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
        }
    }

}
