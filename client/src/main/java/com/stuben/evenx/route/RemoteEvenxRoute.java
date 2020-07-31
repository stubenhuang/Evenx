package com.stuben.evenx.route;

import com.stuben.evenx.invoke.EvenxInvoker;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteEvenxRoute {
    private static Map<String/* topic */ , Map<String/* tag */, EvenxInvoker>> remoteConsumerMap = new ConcurrentHashMap<>();

    public static void add(String topic, String tag, EvenxInvoker evenxInvoker) {
        if (!remoteConsumerMap.containsKey(topic)) {
            remoteConsumerMap.put(topic, new HashMap<>());
        }
        remoteConsumerMap.get(topic).put(tag, evenxInvoker);
    }

    public static Set<String> topics() {
        return remoteConsumerMap.keySet();
    }

    public static Map<String, EvenxInvoker> getInvokerMap(String topic) {
        return remoteConsumerMap.get(topic);
    }
}
