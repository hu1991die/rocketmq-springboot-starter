## rocketmq-spring-boot-starter
> SpringBoot集成RocketMQ消息中间件的Starter插件   


### quick-start

#### 一、生产者端

##### 1.新建一个SpringBoot项目，然后在pom.xml文件引入rocketmq-spring-boot-starter依赖

```
<!-- 引入rocketmq-starter -->
<dependency>
    <groupId>com.feizi.starter</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

##### 2.配置nameserver地址和生产者组实例名称

```
spring.rocketmq.name-server=127.0.0.1:9876
spring.rocketmq.producer-config.group=my_group
```

##### 3.注入`RocketMqSendTemplate` 消息发送模板类

```
@Resource
private RocketMqSendTemplate rocketMqSendTemplate;
```

##### 4.包装MQ消息

```
MessageData<User> messageData2 = new MessageData<>();
messageData2.setUuid(UUID.randomUUID().toString());
messageData2.setTimestamp(System.currentTimeMillis());
User user2 = new User(2, "hello", 26);
messageData2.setData(user2);

Message message1 = MessageUtils.wrapMessage(MQ_TOPIC1, messageData1);
```

**注意**：上面的`User`类定义在starter里面是为了方便测试，实际应用的时候直接定义在业务模块里面就可以了


##### 5.发送MQ消息
```
//发送MQ消息
SendResult sendResult1 = rocketMqSendTemplate.syncSend(message1);
```

##### 生产端完整代码（`MqProducerApplication.java`）：
```
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MqProducerApplication implements CommandLineRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(MqProducerApplication.class);

	/**
	 * MQ消息消费者topic定义
	 */
	private static final String MQ_TOPIC1 = "feizi_topic1";
	private static final String MQ_TOPIC2 = "feizi_topic2";

	@Resource
	private RocketMqSendTemplate rocketMqSendTemplate;

	public static void main(String[] args) {
		SpringApplication.run(MqProducerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		LOGGER.info("================start生产者发送MQ消息");

		//first
		MessageData<User> messageData1 = new MessageData<>();
		messageData1.setUuid(UUID.randomUUID().toString());
		messageData1.setTimestamp(System.currentTimeMillis());
		User user1 = new User(1, "feizi", 25);
		messageData1.setData(user1);

		//second
		MessageData<User> messageData2 = new MessageData<>();
		messageData2.setUuid(UUID.randomUUID().toString());
		messageData2.setTimestamp(System.currentTimeMillis());
		User user2 = new User(2, "hello", 26);
		messageData2.setData(user2);

		//封装MQ消息（first）
		Message message1 = MessageUtils.wrapMessage(MQ_TOPIC1, messageData1);

		//发送MQ消息
		SendResult sendResult1 = rocketMqSendTemplate.syncSend(message1);
		if(null != sendResult1){
			/**
			 * TODO 发送结果，发送完毕执行业务操作
			 */
			LOGGER.info("================sendResult1: " + sendResult1);
		}

		/*##################################################################*/

		//封装MQ消息（second）
		Message message2 = MessageUtils.wrapMessage(MQ_TOPIC2, messageData2);

		//发送MQ消息, 如果失败，则尝试发送3次
		SendResult sendResult2 = rocketMqSendTemplate.syncSend(message2, 3);
		if(null != sendResult2){
			/**
			 * TODO 发送结果，发送完毕执行业务操作
			 */
			LOGGER.info("================sendResult2: " + sendResult2);
		}
		LOGGER.info("================end生产者发送MQ消息");
	}
}
```

##### 配置文件(`application.properties`)：

```
server.port=8084
spring.application.name=mq-producer

spring.rocketmq.name-server=127.0.0.1:9876
spring.rocketmq.producer-config.group=my_group
```

#### 二、消费者端
##### 1.新建一个SpringBoot项目，然后在pom.xml文件引入rocketmq-spring-boot-starter依赖

```
<!-- 引入rocketmq-starter -->
<dependency>
    <groupId>com.feizi.starter</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

##### 2.配置nameserver地址

```
spring.rocketmq.name-server=127.0.0.1:9876
```

##### 3.编写消费者类，直接实现RocketMqConsumerListener消费者监听接口接口

```
@Component
public class MyConsumer1 implements RocketMqConsumerListener<MessageData>{

}
```

**注意**：需要配置`@Component`注解，以便能被spring扫描到.

##### 4.配置消费者注解，指定topic主题，consumerGroup消费者组，tag标签等（根据实际业务需要）

```
@Component
@RocketMqConsumer(topic = "feizi_topic1", consumerGroup = "my_consumer_group1")
public class MyConsumer1 implements RocketMqConsumerListener<MessageData> {

}
```

##### 消费端完整代码：

```
@Component
@RocketMqConsumer(topic = "feizi_topic1", consumerGroup = "my_consumer_group1")
public class MyConsumer1 implements RocketMqConsumerListener<MessageData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumer1.class);

    @Override
    public void consume(MessageData message) {
        LOGGER.info("==========================consumer start=====================");

        if(Objects.isNull(message)){
            //接收到空消息，也表明此次消费成功
            return;
        }

        /**
         * TODO 具体的业务逻辑
         */
        LOGGER.info("MyConsumer1 received message: {}", message);
        LOGGER.info("UUID唯一值，用于消费幂等控制：: {}", message.getUuid());
        LOGGER.info("消息产生时间戳: {}", message.getTimestamp());
        LOGGER.info("消息内容: {}", message.getData());

        //json字符串转Obj
        User user = JsonUtils.jsonStr2Obj(message.getData(), new TypeReference<User>(){});
        LOGGER.info("具体内容: {}", user);
        LOGGER.info("id: {}", user.getId());
        LOGGER.info("name: {}", user.getName());
        LOGGER.info("age: {}", user.getAge());

        LOGGER.info("==========================consumer end=====================");
    }
}
```

**注意**：对于注解RocketMqConsumerListener<T>的泛型参数T，可以指定MessageData消息封装类类型也可以直接指定String类型，建议指定为统一的封装类MessageData类型。

##### 如果将泛型参数T指定为String类型，则代码如下：

```
@Component
@RocketMqConsumer(topic = "feizi_topic2", consumerGroup = "my_consumer_group2")
public class MyConsumer2 implements RocketMqConsumerListener<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumer2.class);

    @Override
    public void consume(String message) {
        LOGGER.info("==========================consumer start=====================");

        if(Objects.isNull(message)){
            //接收到空消息，也表明此次消费成功
            return;
        }

        /**
         * TODO 具体的业务逻辑
         */
        MessageData<User> messageData = JsonUtils.jsonStr2Obj(message, new TypeReference<MessageData<User>>(){});
        LOGGER.info("MyConsumer1 received message: {}", messageData);
        LOGGER.info("UUID唯一值，用于消费幂等控制：: {}", messageData.getUuid());
        LOGGER.info("消息产生时间戳: {}", messageData.getTimestamp());
        LOGGER.info("消息内容: {}", messageData.getData());

        //json字符串转Obj
        User user = JsonUtils.jsonStr2Obj(messageData.getData(), new TypeReference<User>(){});
        LOGGER.info("具体内容: {}", user);
        LOGGER.info("id: {}", user.getId());
        LOGGER.info("name: {}", user.getName());
        LOGGER.info("age: {}", user.getAge());

        LOGGER.info("==========================consumer end=====================");
    }
}
```

我们可以对比下上面将泛型参数T分别指定为MessageData类型和String类型这两种类型的区别。
如果指定为String类型，则接收到的是json字符串，如果需要拿到里面具体的参数就需要额外转换一次。

```
MessageData<User> messageData = JsonUtils.jsonStr2Obj(message, new TypeReference<MessageData<User>>(){});
```

### 三、demo运行效果

1. 生产者端（发送消息端）：
![https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/producer%20.png](https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/producer%20.png)

2. 消费者端（接收消息端）：
![https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/consumer.png](https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/consumer.png)

### 四、demo示例源代码

1. 生产者端（发送消息端）：

github地址：
[https://github.com/hu1991die/mq-producer](https://github.com/hu1991die/mq-producer)

git clone地址：
[git@github.com:hu1991die/mq-producer.git](git@github.com:hu1991die/mq-producer.git)

2. 消费者端（接收消息端）：

github地址：
[https://github.com/hu1991die/mq-consumer](https://github.com/hu1991die/mq-consumer)

git clone地址：
[git@github.com:hu1991die/mq-consumer.git](git@github.com:hu1991die/mq-consumer.git)
