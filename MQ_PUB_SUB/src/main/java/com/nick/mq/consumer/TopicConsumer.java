package com.nick.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * 消费者
 */
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
