package com.stuben.client;

import com.alibaba.fastjson.JSON;
import com.stuben.event.constants.EnvironmentProperties;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

public class EventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    private ApplicationContext applicationContext;
    private DefaultMQPushConsumer mqConsumer;

    public EventConsumer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        Environment environment = applicationContext.getEnvironment();

        String appName = Optional.ofNullable(environment.getProperty(EnvironmentProperties.APP_NAME)).orElse("demo");
        String host = Optional.ofNullable(environment.getProperty(EnvironmentProperties.MQ_HOST)).orElse("127.0.0.1");
        String port = Optional.ofNullable(environment.getProperty(EnvironmentProperties.MQ_PORT)).orElse("9876");

        DefaultMQPushConsumer mqConsumer = new DefaultMQPushConsumer(appName);
        mqConsumer.setNamesrvAddr(host + ":" + port);
        this.mqConsumer = mqConsumer;
    }

    public void startup() {
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(EventController.class);

        if (controllers.isEmpty()) {
            logger.warn("controller is empty");
            return;
        }

        Map<String/*topic*/ , Map<String/*tag*/, ConsumerInvoker>> consumerMap = new HashMap<>();

        for (Object controller : controllers.values()) {
            Class<?> controllerClz = controller.getClass();
            EventController eventController = controllerClz.getAnnotation(EventController.class);

            String topic = eventController.value();

            if (!consumerMap.containsKey(topic)) {
                consumerMap.put(topic, new HashMap<>());
            }

            for (Method method : controllerClz.getDeclaredMethods()) {

                Class<?>[] methodParameterTypes = method.getParameterTypes();
                if (methodParameterTypes.length != 1) {
                    logger.warn("can`t invoke method which params length isn`t one , topic:{} , method:{}", topic, method.getName());
                    continue;
                }

                EventMapping[] methodAnnotationsByType = method.getAnnotationsByType(EventMapping.class);
                if (methodAnnotationsByType.length != 1) {
                    logger.warn("can`t invoke method which annotation length isn`t one , topic:{} , method:{}", topic, method.getName());
                    continue;
                }

                String tag = methodAnnotationsByType[0].value();
                Class<?> paramType = methodParameterTypes[0];

                consumerMap.get(topic).put(tag, ConsumerInvoker.init(controller, method.getName(), paramType));

            }
        }

        for (String topic : consumerMap.keySet()) {
            Map<String, ConsumerInvoker> consumerInvokerMap = consumerMap.get(topic);

            if (consumerInvokerMap.isEmpty()) {
                continue;
            }

            try {
                mqConsumer.subscribe(topic, StringUtils.join(consumerInvokerMap.keySet(), "||"));
            } catch (Exception e) {
                logger.error("subcribe topic:{} error", topic, e);
            }
        }

        mqConsumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
            for (MessageExt msg : messages) {
                Map<String, ConsumerInvoker> consumerInvokerMap = consumerMap.get(msg.getTopic());
                ConsumerInvoker consumerInvoker = consumerInvokerMap.get(msg.getTags());
                consumerInvoker.invoke(msg.getBody());
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        try {
            mqConsumer.start();
        } catch (Exception e) {
            logger.error("event consumer startup failed", e);
        }

    }

    private static class ConsumerInvoker {
        private Object controller;
        private String methodName;
        private Class<?> paramType;

        public static ConsumerInvoker init(Object controller, String methodName, Class<?> paramType) {
            return new ConsumerInvoker(controller, methodName, paramType);
        }

        private ConsumerInvoker(Object controller, String methodName, Class<?> paramType) {
            this.controller = controller;
            this.methodName = methodName;
            this.paramType = paramType;
        }

        public void invoke(byte[] datas) {
            try {
                if (paramType.equals(String.class)) {
                    MethodUtils.invokeMethod(controller, methodName, new String(datas));
                } else {
                    MethodUtils.invokeMethod(controller, methodName, JSON.parseObject(datas, paramType));
                }
            } catch (Exception e) {
                logger.warn("invoke error, datas:{}", new String(datas));
            }
        }

    }

}
