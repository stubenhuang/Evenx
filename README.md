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
