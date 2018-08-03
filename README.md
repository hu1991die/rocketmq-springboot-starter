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
//封装MQ消息，可以指定topic主题，tag子主题，和key唯一识别码
Message message = MessageUtils.buildMessage("feizi_topic", "tagA", "key001", new User(1, "feizi", 25));
```

**注意**：上面的`User`类定义在starter里面是为了方便测试，实际应用的时候直接定义在业务模块里面就可以了


##### 5.发送MQ消息
```
//发送MQ消息
SendResult sendResult = rocketMqSendTemplate.syncSend(message, 3);
```

##### 生产端完整代码（`MqProducerApplication.java`）：
```
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MqProducerApplication implements CommandLineRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(MqProducerApplication.class);

	/**
	 * MQ消息消费者topic定义
	 */
	private static final String MQ_TOPIC = "feizi_topic";

	@Resource
	private RocketMqSendTemplate rocketMqSendTemplate;

	public static void main(String[] args) {
		SpringApplication.run(MqProducerApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		LOGGER.info("================start生产者发送MQ消息");

		/*############################first###############################*/
		//封装MQ消息（first）
		Message message1 = MessageUtils.buildMessage(MQ_TOPIC, "tagA", "key001", new User(1, "feizi", 25));
		sendMessage(message1);

		/*############################second###############################*/
		//封装MQ消息（second）
		Message message2 = MessageUtils.buildMessage(MQ_TOPIC, "tagB", "key002", new User(2, "hello", 26));
		sendMessage(message2);

		LOGGER.info("================end生产者发送MQ消息");
	}

	/*发送MQ消息*/
	private void sendMessage(Message message){
		/* 发送MQ消息, 如果失败，则尝试发送3次 */
		SendResult sendResult = rocketMqSendTemplate.syncSend(message, 3);
		if(null != sendResult){
			/**
			 * TODO 发送结果，发送完毕执行业务操作
			 */
			LOGGER.info("================sendResult: " + sendResult);
		}
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
public class MyConsumer1 implements RocketMqConsumerListener<Message>{

}
```

**注意**：需要配置`@Component`注解，以便能被spring扫描到.

##### 4.配置消费者注解，指定topic主题，consumerGroup消费者组，tag标签等（根据实际业务需要）

```
@Component
@RocketMqConsumer(topic = "feizi_topic", selectorExpress = "tagA", consumerGroup = "my_consumer_group1")
public class MyConsumer1 implements RocketMqConsumerListener<Message> {

}
```

##### 消费端完整代码：

```
@Component
@RocketMqConsumer(topic = "feizi_topic", selectorExpress = "tagA", consumerGroup = "my_consumer_group1")
public class MyConsumer1 implements RocketMqConsumerListener<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyConsumer1.class);

    @Override
    public boolean consume(Message message) {
        LOGGER.info("==========================consumer start=====================");

        if(Objects.isNull(message)){
            //接收到空消息，也表明此次消费成功
            return true;
        }
        LOGGER.info("MyConsumer received message: {}", message);
        LOGGER.info("消息主题topic: {}", message.getTopic());
        LOGGER.info("消息子主题tags: {}", message.getTags());
        LOGGER.info("消息keys: {}", message.getKeys());

        //取出消息体
        byte[] messageBody = message.getBody();
        if(Objects.isNull(messageBody)){
            //接收到空消息，也表明此次消费成功
            return true;
        }

        /**
         * TODO 具体的业务逻辑
         */

        String messageStr = StringUtils.byteArr2Str(messageBody);
        MessageData<User> messageData = JsonUtils.jsonStr2Obj(messageStr, new TypeReference<MessageData<User>>(){});
        LOGGER.info("UUID唯一值，用于消费幂等控制：: {}", messageData.getUuid());
        LOGGER.info("消息产生时间戳: {}", messageData.getTimestamp());

        //json字符串转Obj
        User user = JsonUtils.jsonObj2Obj(messageData.getData(), new TypeReference<User>(){});
        LOGGER.info("消息内容: {}", user);
        LOGGER.info("id: {}", user.getId());
        LOGGER.info("name: {}", user.getName());
        LOGGER.info("age: {}", user.getAge());

        LOGGER.info("==========================consumer end=====================");
        return true;
    }
}
```

**注意**：对于注解RocketMqConsumerListener的泛型参数T，可以指定为Message类型，也可以指定MessageData消息封装类类型，同时还可以直接指定String类型，建议统一指定为`Message`类型，因为这样我们可以直接拿到`topic`主题，`tag`子主题和`key`唯一识别码。

### 三、demo运行效果

1. 生产者端（发送消息端）：
![https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/producer.png](https://github.com/hu1991die/rocketmq-springboot-starter/blob/master/img/producer.png)

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
