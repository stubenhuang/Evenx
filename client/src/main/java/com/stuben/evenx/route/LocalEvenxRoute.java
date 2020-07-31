package com.stuben.evenx.route;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.stuben.evenx.invoke.EvenxInvoker;
import com.stuben.evenx.protocol.BaseEvenx;

public class LocalEvenxRoute {
    private static Map<String/* evenx name */, EvenxInvoker> localConsumerMap = new ConcurrentHashMap<>();

    public static void put(String evenxName, EvenxInvoker evenxInvoker) {
        localConsumerMap.put(evenxName, evenxInvoker);
    }

    public static void post(BaseEvenx evenx) {
        String evenxName = evenx.getClass().getName();

        if (localConsumerMap.containsKey(evenxName)) {
            localConsumerMap.get(evenxName).invoke(evenx);
        }
    }
}
