package com.stuben.client;

import com.stuben.event.BaseEvent;
import com.stuben.event.EventDeclare;
import com.stuben.serializer.FastJsonSerializer;
import com.stuben.serializer.ISerializer;
import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class EventProducer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService executorService;
    private ApplicationContext applicationContext;
    private String source;
    private DefaultMQProducer producer;

    private ThreadLocal<Set<String>> cycleEvents = ThreadLocal.withInitial(LinkedHashSet::new);//avoid cycle event
    private Map<Class, EventDeclare> declareCache = new ConcurrentHashMap<>();
    private Map<Class, ISerializer> serializerCache = new ConcurrentHashMap<>();

    EventProducer(ExecutorService executorService, ApplicationContext applicationContext, String source,
        String appName, String host, String port) {
        this.executorService = executorService;
        this.applicationContext = applicationContext;
        this.source = source;

        this.producer = new DefaultMQProducer(appName);
        this.producer.setNamesrvAddr(host + ":" + port);
        try {
            this.producer.start();
        } catch (Exception e) {
            logger.error("producer start failed , so it can not send shared msg");
            this.producer = null;
        }
    }

    public void asyncPost(BaseEvent event) {
        Set<String> events = new LinkedHashSet<>(cycleEvents.get());
        cycleEvents.get().clear();

        executorService.submit(() -> {
            boolean isCycle = !events.add(event.getClass().getSimpleName());

            if (isCycle) {
                logger.error("cycle event , interrupt , event now:{} , events:{}", event.getClass(), events);
                cycleEvents.get().clear();
                return;
            }

            cycleEvents.set(events);

            logger.debug("publish event , event now :{} , events:{}", event.getClass(), events);

            applicationContext.publishEvent(event);

            share(event);
        });
    }

    private void share(BaseEvent event) {
        if (null != producer) {
            EventDeclare eventDeclare = getDeclare(event);

            if (eventDeclare.share()) {
                ISerializer serializer = getSerializer(eventDeclare);
                Message msg = new Message(eventDeclare.source(), eventDeclare.name(), serializer.encode(event));
                try {
                    producer.send(msg);
                } catch (Exception e) {
                    logger.error("share event : {} failed", event, e);
                }
            }
        }
    }

    private ISerializer getSerializer(EventDeclare eventDeclare) {
        return serializerCache.computeIfAbsent(eventDeclare.serializer(), clz -> {
            try {
                return (ISerializer) clz.newInstance();
            } catch (Exception e) {
                logger.error("get serializer failed,class:{} , use fastjson default", clz);
                return new FastJsonSerializer();
            }
        });
    }

    private EventDeclare getDeclare(BaseEvent event) {
        return declareCache.computeIfAbsent(event.getClass(), key -> {
            EventDeclare declaredAnnotation = (EventDeclare) key.getDeclaredAnnotation(EventDeclare.class);

            if (null == declaredAnnotation) {
                return new EventDeclare() {
                    @Override public boolean share() {
                        return false;
                    }

                    @Override public String source() {
                        return source;
                    }

                    @Override public String name() {
                        return key.getName();
                    }

                    @Override public Class<? extends ISerializer> serializer() {
                        return FastJsonSerializer.class;
                    }

                    @Override public Class<? extends Annotation> annotationType() {
                        return this.getClass();
                    }
                };
            }

            return declaredAnnotation;

        });
    }

}
