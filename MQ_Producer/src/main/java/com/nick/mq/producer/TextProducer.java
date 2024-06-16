package com.nick.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.Random;

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
