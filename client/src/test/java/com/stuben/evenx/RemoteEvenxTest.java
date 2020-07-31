package com.stuben.evenx;

import org.junit.Test;

import com.stuben.evenx.config.EvenxConsumerConfig;
import com.stuben.evenx.config.EvenxProducerConfig;
import com.stuben.evenx.consumer.EvenxConsumerLauncher;
import com.stuben.evenx.evenx.RemoteEvenx;
import com.stuben.evenx.producer.EvenxProducer;

public class RemoteEvenxTest {
    @Test
    public void test() throws InterruptedException {
        // init consumer
        EvenxConsumerConfig consumerConfig = new EvenxConsumerConfig();
        consumerConfig.setAppName("remote");
        consumerConfig.setMqHost("127.0.0.1");
        consumerConfig.setMqPort(9876);
        consumerConfig.setThreadNum(5);
        consumerConfig.setScanPackage("com.stuben.evenx.consumer.RemoteConsumerAction");
        EvenxConsumerLauncher evenxConsumerLauncher = new EvenxConsumerLauncher(consumerConfig);
        evenxConsumerLauncher.startup();

        // produce
        EvenxProducerConfig producerConfig = new EvenxProducerConfig();
        producerConfig.setAppName("local");
        producerConfig.setMqHost("127.0.0.1");
        producerConfig.setMqPort(9876);
        producerConfig.setThreadNum(5);
        EvenxProducer producer = new EvenxProducer(producerConfig);

        RemoteEvenx remoteEvenx = new RemoteEvenx();
        remoteEvenx.setContent("Hello World");
        producer.produce(remoteEvenx);


        Thread.sleep(10000);
    }
}
