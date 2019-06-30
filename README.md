## EVENT DEMO (Project name isn`t finished)

> Highly abstracted event system which can make business code easy.

#### Quick start

1. Set properties( See : com.stuben.event.constants.EnvironmentProperties) , and then load them into spring application context.

2. New a event
``` java
public class DemoEvent extends BaseEvent{
    //something
}

```

2. Use producer
``` java
EventProducer producer = new EventProducer(applicationContext);
producer.asyncPost(new DemoEvent());
```

3. Startup consumer
``` java
EventConsumer consumer = new EventConsumer(applicationContext);
consumer.startup();
```

4. Use consumer
``` java
@EventController("demo") // producer app name
public class DemoController{
    @EventMapping("xxx.DemoEvent") // event class full name
    public void demo(String demoEventJSON){
        // do something
    }
}
```

#### TODO
1. Event doesn`t use 'abstract class' to limit . Can it use annotation?
2. Can use annotation to produce event after a method finish?
3. Can consumer be unified whether local or remote ? Just use @EventControll . 
4. Rocketmq is a persistent message system so it is too heavy. Can i just use a simple route table to share my event? 
