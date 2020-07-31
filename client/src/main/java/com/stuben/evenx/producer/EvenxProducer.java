package com.stuben.evenx.producer;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stuben.evenx.config.EvenxProducerConfig;
import com.stuben.evenx.protocol.BaseEvenx;
import com.stuben.evenx.protocol.EvenxDeclare;
import com.stuben.evenx.route.LocalEvenxRoute;
import com.stuben.evenx.seriallizer.FastJsonSerializer;
import com.stuben.evenx.seriallizer.ISerializer;

public class EvenxProducer {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ExecutorService executorService;
    private DefaultMQProducer producer;
    private String appName;

    private ThreadLocal<Set<String>> cycleEvenxs = ThreadLocal.withInitial(LinkedHashSet::new);// avoid cycle evenx
    private Map<Class, EvenxDeclare> declareCache = new ConcurrentHashMap<>();
    private Map<Class, ISerializer> serializerCache = new ConcurrentHashMap<>();

    public EvenxProducer(EvenxProducerConfig config) {

        this.executorService = Executors.newFixedThreadPool(config.getThreadNum());

        this.appName = config.getAppName();

        this.producer = new DefaultMQProducer(appName);
        this.producer.setNamesrvAddr(config.getMqHost() + ":" + config.getMqPort());
        try {
            this.producer.start();
        } catch (Exception e) {
            logger.error("evenx start failed , so it can not send shared msg");
            this.producer = null;
        }
    }

    public void produce(BaseEvenx evenx) {
        Set<String> evenxs = new LinkedHashSet<>(cycleEvenxs.get());
        cycleEvenxs.get().clear();

        executorService.submit(() -> {
            boolean isCycle = !evenxs.add(evenx.getClass().getSimpleName());

            if (isCycle) {
                logger.error("cycle evenx , interrupt , evenx now:{} , evenxs:{}", evenx.getClass(), evenxs);
                cycleEvenxs.get().clear();
                return;
            }

            cycleEvenxs.set(evenxs);

            logger.debug("publish evenx , evenx now :{} , evenxs:{}", evenx.toString(), evenxs.toString());

            LocalEvenxRoute.post(evenx);

            share(evenx);
        });
    }

    private void share(BaseEvenx evenx) {
        if (null != producer) {
            EvenxDeclare evenxDeclare = getDeclare(evenx);

            if (evenxDeclare.share()) {
                ISerializer serializer = getSerializer(evenxDeclare);

                String source = StringUtils.isBlank(evenxDeclare.source()) ? appName : evenxDeclare.source();
                String evenxName = StringUtils.isBlank(evenxDeclare.name()) ? evenx.getClass().getName() : evenxDeclare.name();

                Message msg = new Message(source, evenxName, serializer.encode(evenx));
                try {
                    SendResult sendResult = producer.send(msg);
                    if (sendResult.getSendStatus() != SendStatus.SEND_OK) {
                        logger.error("mq send error, evenx:{} , status:{}", evenx, sendResult.getSendStatus());
                    }
                } catch (Exception e) {
                    logger.error("share evenx : {} failed", evenx, e);
                }
            }
        }
    }

    private ISerializer getSerializer(EvenxDeclare evenxDeclare) {
        return serializerCache.computeIfAbsent(evenxDeclare.serializer(), clz -> {
            try {
                return (ISerializer) clz.newInstance();
            } catch (Exception e) {
                logger.error("get serializer failed,class:{} , use fastjson default", clz);
                return new FastJsonSerializer();
            }
        });
    }

    private EvenxDeclare getDeclare(BaseEvenx evenx) {
        return declareCache.computeIfAbsent(evenx.getClass(), key -> {
            EvenxDeclare declaredAnnotation = (EvenxDeclare) key.getDeclaredAnnotation(EvenxDeclare.class);

            if (null == declaredAnnotation) {
                return new EvenxDeclare() {
                    @Override
                    public boolean share() {
                        return false;
                    }

                    @Override
                    public String source() {
                        return appName;
                    }

                    @Override
                    public String name() {
                        return key.getName();
                    }

                    @Override
                    public Class<? extends ISerializer> serializer() {
                        return FastJsonSerializer.class;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return this.getClass();
                    }
                };
            }

            return declaredAnnotation;

        });
    }

}
