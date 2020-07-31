package com.stuben.evenx.consumer;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stuben.evenx.config.EvenxConsumerConfig;
import com.stuben.evenx.invoke.EvenxInvoker;
import com.stuben.evenx.route.LocalEvenxRoute;
import com.stuben.evenx.route.RemoteEvenxRoute;

public class EvenxConsumerLauncher {
    private static final Logger logger = LoggerFactory.getLogger(EvenxConsumerLauncher.class);

    private DefaultMQPushConsumer mqConsumer;
    private String appName;
    private String scanPackage;
    private ExecutorService executorService;

    public EvenxConsumerLauncher(EvenxConsumerConfig config) {
        this.appName = config.getAppName();

        DefaultMQPushConsumer mqConsumer = new DefaultMQPushConsumer(config.getAppName());
        mqConsumer.setNamesrvAddr(config.getMqHost() + ":" + config.getMqPort());
        this.mqConsumer = mqConsumer;

        this.scanPackage = config.getScanPackage();

        this.executorService = Executors.newFixedThreadPool(config.getThreadNum());
    }

    public void startup() {
        Set<Class<?>> controllers = new Reflections(scanPackage).getTypesAnnotatedWith(EvenxConsumerController.class);

        if (controllers.isEmpty()) {
            logger.warn("controller is empty");
            return;
        }


        initConsumerMap(controllers);

        startupRemoteConsumer();
    }

    private void startupRemoteConsumer() {
        for (String topic : RemoteEvenxRoute.topics()) {
            Map<String, EvenxInvoker> consumerInvokerMap = RemoteEvenxRoute.getInvokerMap(topic);

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

                executorService.submit(() -> {
                    Map<String, EvenxInvoker> consumerInvokerMap = RemoteEvenxRoute.getInvokerMap(msg.getTopic());
                    if (null == consumerInvokerMap) {
                        logger.warn("consumerInvokerMap is null , topic:{}", msg.getTopic());
                        return;
                    }

                    EvenxInvoker evenxInvoker = consumerInvokerMap.get(msg.getTags());
                    if (null == evenxInvoker) {
                        logger.warn("evenxInvoker is null , topic:{} , tag:{}", msg.getTopic(), msg.getTags());
                        return;
                    }

                    evenxInvoker.invoke(msg.getBody());
                });
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        try {
            mqConsumer.start();
        } catch (Exception e) {
            logger.error("evenx consumer startup failed", e);
        }
    }

    private void initConsumerMap(Set<Class<?>> controllers) {
        for (Class<?> controllerClz : controllers) {
            EvenxConsumerController evenxConsumerController = controllerClz.getAnnotation(EvenxConsumerController.class);

            String topic = evenxConsumerController.value();

            boolean isLocal = StringUtils.isBlank(topic) || topic.equals(appName);

            for (Method method : controllerClz.getDeclaredMethods()) {

                Class<?>[] methodParameterTypes = method.getParameterTypes();
                if (methodParameterTypes.length != 1) {
                    logger.warn("can`t invoke method which params length isn`t one , topic:{} , method:{}", topic, method.getName());
                    continue;
                }

                EvenxConsumerMapping[] methodAnnotationsByType = method.getAnnotationsByType(EvenxConsumerMapping.class);
                if (methodAnnotationsByType.length != 1) {
                    logger.warn("can`t invoke method which annotation length isn`t one , topic:{} , method:{}", topic, method.getName());
                    continue;
                }

                String mapping = methodAnnotationsByType[0].value();
                Class<?> paramType = methodParameterTypes[0];

                EvenxInvoker evenxInvoker;
                try {
                    evenxInvoker = EvenxInvoker.init(controllerClz.newInstance(), method, paramType);
                } catch (InstantiationException | IllegalAccessException e) {
                    logger.error("EvenxInvoker init failed , clz:{}", controllerClz);
                    continue;
                }

                if (isLocal) {
                    String evenxName = null;
                    if (StringUtils.isNotBlank(mapping)) {
                        try {
                            Class.forName(mapping);
                            evenxName = mapping;
                        } catch (ClassNotFoundException e) {
                            logger.error("Can not load mapping in local. mapping:{}", mapping);
                        }

                    } else {
                        evenxName = paramType.getName();
                    }

                    if (StringUtils.isNotBlank(evenxName)) {
                        LocalEvenxRoute.put(evenxName, evenxInvoker);
                    }

                } else {
                    RemoteEvenxRoute.add(topic, mapping, evenxInvoker);
                }

            }
        }
    }

}
