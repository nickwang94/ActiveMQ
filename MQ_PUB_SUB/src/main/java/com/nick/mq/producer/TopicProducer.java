package com.nick.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 发布一串文本消息到ActiveMQ中
 */
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
