package com.nick.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

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
