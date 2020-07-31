package com.stuben.evenx;

import org.junit.Test;

import com.stuben.evenx.config.EvenxConsumerConfig;
import com.stuben.evenx.config.EvenxProducerConfig;
import com.stuben.evenx.consumer.EvenxConsumerLauncher;
import com.stuben.evenx.evenx.LocalEvenx;
import com.stuben.evenx.producer.EvenxProducer;

public class LocalEvenxTest {
    @Test
    public void test() throws InterruptedException {
        // init consumer
        EvenxConsumerConfig consumerConfig = new EvenxConsumerConfig();
        consumerConfig.setAppName("local");
        consumerConfig.setMqHost("127.0.0.1");
        consumerConfig.setMqPort(9876);
        consumerConfig.setThreadNum(5);
        consumerConfig.setScanPackage("com.stuben.evenx.consumer.LocalConsumerAction");
        EvenxConsumerLauncher evenxConsumerLauncher = new EvenxConsumerLauncher(consumerConfig);
        evenxConsumerLauncher.startup();

        // produce
        EvenxProducerConfig producerConfig = new EvenxProducerConfig();
        producerConfig.setAppName("local");
        producerConfig.setMqHost("127.0.0.1");
        producerConfig.setMqPort(9876);
        producerConfig.setThreadNum(5);
        EvenxProducer producer = new EvenxProducer(producerConfig);

        LocalEvenx localEvenx = new LocalEvenx();
        localEvenx.setContent("Hello World");
        producer.produce(localEvenx);

        Thread.sleep(2000);
    }
}
