## EVENX

> Highly abstracted event system which can make coding easy.

#### Quick start

1. New a evenx
``` java
@EvenxDeclare // it isn`t necessary
public class DemoEvenx extends BaseEvenx{
    //something
}
```

2. Use consumer
``` java
@EvenxConsumerController("demo") // producer app name. If value isn`t set , evenx will use local app name.
public class DemoController{
    @EvenxConsumerMapping("xxx.DemoEvenx") // evenx class full name. If value isn`t set , evenx will use param class name.
    public void demo(String demoEvenxJSON){
        // do something
    }
}
```

3. Startup consumer
``` java
EvenxConsumerConfig consumerConfig = new EvenxConsumerConfig();
consumerConfig.setAppName("local");
consumerConfig.setMqHost("127.0.0.1");
consumerConfig.setMqPort(9876);
consumerConfig.setThreadNum(5);
consumerConfig.setScanPackage("com.stuben.evenx.consumer.LocalConsumerAction");

EvenxConsumerLauncher consumerLauncher = new EvenxConsumerLauncher();
consumerLauncher.startup();
```

4. Use producer
``` java
EvenxProducerConfig producerConfig = new EvenxProducerConfig();
producerConfig.setAppName("local");
producerConfig.setMqHost("127.0.0.1");
producerConfig.setMqPort(9876);
producerConfig.setThreadNum(5);

EvenxProducer producer = new EvenxProducer(producerConfig);
producer.produce(new DemoEvenx());
```



#### TODO
1. Evenx doesn`t use 'abstract class' to limit . Can it use annotation?
2. Can use annotation to produce evenx after a method finish?
3. Rocketmq is a persistent message system so it is too heavy. Can i just use a simple route table to share evenx? 
4. Finish the test case.
5. Use remote seriallizer.
6. Mq producer and consumer init outside.
