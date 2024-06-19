# Active MQ

## Introduction

### JMS

> Java Message Service



### MOM

> Message Oriented Middleware



- Apache Active MQ

- Alibaba Rocket MQ

- IBM MQ Series

- Microsoft MSMQ

- BEA Rabbit MQ (不完全支持)

基于JMS实现的MOM又被称为JMS Provider

### Message

> 是两台计算机间传输的数据单位

A -> MOM -> B

### 主要解决问题

> A把消息发给MQ，B从MQ去消息

- 解决同步转异步，提高客户端处理效率
- 实现分流，多任务多个线程/进程从MOM去消息并执行

## Active MQ

> Apache 对MOM的一个具体实现

- 支持Java，C，C++， C#, Ruby, Perl, Python, PHP
- 支持OpenWire，Stomp，REST，WS，Notification，XMPP，AMQP
- 支持JMS1.1, J2EE 1.4
- 支持JDBC和journal提供高速消息持久化

### Destination

> 目的地，JMS Provider 负责维护，用于对Message进行管理的对象，MessageProducer需要指定Destination才能发送消息，MessageConsumer需要指定Destination才能接受消息

### Producer

> 消息的生成者，负责发送Message到目的地，应用接口为MessageProducer

### Consumer

> 消息消费者，负责从目的地中消费（处理，监听，订阅）Message，应用接口为MessageConsumer

### Message

> 消息，消息封装一次通话的内容，常见类型有：StreamingMesssage，BytesMessage，TextMessage，ObjectMessage，MapMessage

### ConnectionFactory

> 连接工厂，用于创建链接的工厂类型

### Connection

> 链接，用户建立访问呢ActiveMQ链接的类型，有链接工厂创建

### Sesison

> 会话，一次持久有效有状态的访问，由链接创建，具体操作消息的基础支撑

### Queue & Topic

> Queue是队列目的地，队列中的消息默认只能由唯一一个消费者处理，一旦处理消息删除
> Topic是主题目的地，对发送给所有的消费者同时处理，只有在消息可以重复处理的业务场景中使用
> 他们都是Destination的子接口

### PTP

> Point 2 point, 点对点消息模型

### Pub & Sub

> Publish & Subscribe

## Active MQ 安装

> [Active MQ Java 8 Windows Download Link](https://activemq.apache.org/components/classic/documentation/download-archives)

## Active MQ 应用

### P2P处理模式 （Queue）

消息生产者发送消息到queue中，然后消费者从queue中取出并消费消息。
消息被消费后，queue中不再保存消息，所以消费者不可能消费已经被消费的消息。
Queue存在多个消费者，但是一个消息只会有一个消费者消费。
当消费者不存在时，消息会一直保存，知道由消费者来消费。

### Publish & Subscribe 处理模式 （Topic）

生产者将消息发布到topic中，同时有多个

# PTP Demo (1 Provider & 1 Consumer)

## Provider

```java
@Slf4j
public class TextProducer {
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String PUBLISH_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Queue destination;
    private MessageProducer producer;
    private TextMessage textMessage;

    public TextProducer() {
        factory = new ActiveMQConnectionFactory(USER_NAME, PASSWORD, BROKER_URL);
    }

    public void sendTextMessage(String message) {
        try {
            connection = factory.createConnection();
            connection.start();
            /**
             * acknowledgeMode:
             * AUTO_ACKNOWLEDGE     自动确认消息，消费者处理后，自动确认，常用
             * CLIENT_ACKNOWLEDGE   客户端手动确认
             * DUPS_OK_ACKNOWLEDGE  有副本的客户端手动确认，一个消息可以多次处理，不建议使用
             */
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            destination = session.createQueue(PUBLISH_QUEUE_NAME);
            producer = session.createProducer(destination);
            textMessage = session.createTextMessage(message);
            producer.send(textMessage);
            log.info("Message Sent Successfully");
        } catch (JMSException e) {
            log.error("Error when sending JMS to Queue: {}", e);
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                    connection.close();
                    log.info("Closed MQ Producer Successfully");
                } catch (JMSException e) {
                    log.error("Error when closing Producer: {}", e);
                }
            }
        }
    }
}
```

## Consumer

```
@Slf4j
public class TextConsumer {
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String RECEIVE_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Queue destination;
    private MessageConsumer consumer;
    private TextMessage message;
    private String receivedMessage;

    public TextConsumer() {
        factory = new ActiveMQConnectionFactory(USER_NAME, PASSWORD, BROKER_URL);
    }

    public String receiveTextMessage() {
        try {
            connection = factory.createConnection();
            // 消息的消费者必须主动启动链接，否则无法处理消息
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            destination = session.createQueue(RECEIVE_QUEUE_NAME);
            consumer = session.createConsumer(destination);
            // receive方法是一个主动获取消息的方法，执行一次，拉去一个消息，学习使用，开发一般不用
            message = (TextMessage) consumer.receive();
            receivedMessage = message.getText();
            log.info("Received Message: {}", receivedMessage);
        } catch (JMSException e) {
            log.error("Error when receiving message: {}", e);
        } finally {
            if (consumer != null) {
                try {
                    consumer.close();
                    connection.close();
                    log.info("Closed Consumer Successfully");
                } catch (JMSException e) {
                    log.error("Error when closing consumer: {}", e);
                }
            }
        }
        return receivedMessage;
    }
}
```

# Consumer Listener

```
@Slf4j
public class ConsumerListener {
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer messageConsumer;

    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String RECEIVE_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    public void consumerMessage() {
        connectionFactory = new ActiveMQConnectionFactory(USER_NAME, PASSWORD, BROKER_URL);
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            destination = session.createQueue(RECEIVE_QUEUE_NAME);
            messageConsumer = session.createConsumer(destination);

            messageConsumer.setMessageListener(new MessageListener() {
                /**
                 * 监听器一旦注册，永久有效 （consumer线程不关闭）
                 * 消息处理方式：只要有消息未处理，自动调用onMessage方法处理消息
                 * 监听器可以注册若干，注册多个监听器，相当于集群
                 * ActiveMQ自动的循环调用多个监听器，处理队列消息，实现并行
                 * @param message 待处理消息
                 */
                @Override
                public void onMessage(Message message) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    try {
                        // acknowledge method 代表consumer已经收到消息，确定后MQ删除对应的消息
                        message.acknowledge();
                        Object object = objectMessage.getObject();
                        log.info("Message in Listener: {}", object.toString());
                    } catch (JMSException e) {
                        log.error("JMSException in Listener: {}", e);
                    }
                }
            });

            // 这里等待输入，是为了保证Listener代码不结束，否则监听器自动关闭
            System.in.read();
        } catch (JMSException e) {
            log.error("JMSException: {}", e);
        } catch (IOException e) {
            log.error("IOException: {}", e);
        }
    }
}
```

# Pub / Sub

> Pub将消息发布到Topic中，同时有多个消费者订阅消费该消息
> 和P2P不同，发布到Topic消息会被所有订阅者消费
> 当生产者发布消息，不管是否有消费者，都不会保留消息

## Pub

```java
@Slf4j
public class TopicProducer {
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String PUBLISH_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Destination destination;
    private Session session;
    private MessageProducer messageProducer;
    private Message message;

    /**
     * 发送消息到ActiveMQ中，具体消息内容为参数信息
     * 开发JMS相关代码过程中，接口类型都是javax.jms包
     * @param dataText
     */
    public void sendTextMessage(String dataText) {
        // 创建链接工厂，链接ActiveMQ服务的连接工厂
        connectionFactory = new ActiveMQConnectionFactory(USER_NAME, PASSWORD, BROKER_URL);
        try {
            // 通过工厂，创建连接对象
            // 方法有重载，不写账号密码则使用工厂的
            connection = connectionFactory.createConnection();
            // 建议启动连接，不是必须的
            connection.start();
            // 通过连接对象创建会话对象, 之后所有的操作都是基于会话的
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // 通过会话创建topic, producer, message
            destination = session.createTopic(PUBLISH_QUEUE_NAME);
            messageProducer = session.createProducer(destination);
            message = session.createTextMessage(dataText);
            // 最后通过producer来发送message
            messageProducer.send(message);
            log.info("Message has been sent: {}", dataText);
        } catch (JMSException e) {
            log.error("JMSException: {}", e);
        } finally {
            if (messageProducer != null) {
                try {
                    messageProducer.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing producer: {}", e);
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing session: {}", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing connection: {}", e);
                }
            }
        }
    }

    public static void main(String[] args) {
        TopicProducer topicProducer = new TopicProducer();
        topicProducer.sendTextMessage("Test Topic on Active MQ");
    }
}
```

### Sub

```java
@Slf4j
public class TopicConsumer {
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private Destination destination;
    private MessageConsumer messageConsumer;
    private Message message;

    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String RECEIVE_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    public String receiveTextMessage() {
        String receivedTextMessage = "";
        connectionFactory = new ActiveMQConnectionFactory(USER_NAME, PASSWORD, BROKER_URL);
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            destination = session.createTopic(RECEIVE_QUEUE_NAME);
            messageConsumer = session.createConsumer(destination);
            message = messageConsumer.receive();
            message.acknowledge();
            receivedTextMessage = ((TextMessage) message).getText();
        } catch (JMSException e) {
            log.error("JMSException: {}", e);
        } finally {
            if (messageConsumer != null) {
                try {
                    messageConsumer.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing consumer: {}", e);
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing session: {}", e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.error("JMSException when closing connection: {}", e);
                }
            }
        }
        return receivedTextMessage;
    }

    public static void main(String[] args) {
        TopicConsumer topicConsumer = new TopicConsumer();
        String receivedTextMessage = topicConsumer.receiveTextMessage();
        log.info("Received Text Message: {}", receivedTextMessage);
    }
}
```

# P2P vs. Pub&Sub

|          | Topic                                        | Queue                                                                     |
| -------- | -------------------------------------------- | ------------------------------------------------------------------------- |
| 概要       | 发布订阅消息                                       | 点对点                                                                       |
| 有无状态     | topic数据默认无状态                                 | Queue数据默认在MQ服务器上以文件存储                                                     |
| 完整性保障    | 不保证Pub发布数据，Sub都能接受到                          | Queue保证每条数据都能被Receiver接受                                                  |
| 消息是否丢失   | Pub消息到topic，只有正在监听该topic的Sub能接受，如果没有该topic丢失 | Sender发送消息到目标Queue，Receiver可以异步接受Queue上的消息。Queue的消息如果暂时没有Receiver接受，也不会丢失 |
| 消息发布接收策略 | 一对多，监听相同topic的多个Sub都能收到消息，Sub接受完通知MQ         | 一对一，Sender发送消息，只能有一个Receiver接受，并通知MQ服务器已接受，MQ对Queue的消息进行删除                |

# Active MQ Security

Active MQ提供安全认证，用户名密码登录规则，如果需要使用安全认证，必须在核心配置文件中开启安全配置。

在 `conf/activemq.xml`中配置文件的`<broker>`标签中增加下述内容:

```xml
<!--
            > 表示所有的
            admins 表示用户组
            ActiveMQ.Advisory.> 系统自带的, 其中>代表任意字符
        -->
<plugins>
  <jaasAuthenticationPlugin configuration="activemq" />
  <authorizationPlugin>
    <map>
      <authorizationMap>
        <authorizationEntries>
          <authorizationEntry topic=">" read="admins" write="admins" admin="admins"/>
          <authorizationEntry queue=">" read="admins" write="admins" admin="admins"/>
          <authorizationEntry topic="ActiveMQ.Advisory.>" read="admins" write="admins" admin="admins"/>
          <authorizationEntry queue="ActiveMQ.Advisory.>" read="admins" write="admins" admin="admins"/>
        </authorizationEntries>
      </authorizationMap>
    </map>
  </authorizationPlugin>
</plugins>
```
