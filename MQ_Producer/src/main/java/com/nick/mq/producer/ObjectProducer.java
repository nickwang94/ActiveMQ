package com.nick.mq.producer;

import lombok.extern.slf4j.Slf4j;

import javax.jms.*;
import java.util.Random;

@Slf4j
public class ObjectProducer {
    private static final String USER_NAME = "admin";
    private static final String PASSWORD = "admin";
    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String PUBLISH_QUEUE_NAME = "PUB.QUEUE.HUB.1.0";

    private ConnectionFactory factory;
    private Connection connection;
    private Session session;
    private Queue destination;
    private MessageProducer producer;
    private ObjectMessage objectMessage;

    public void sendObjectMessageInLoop() {
        try {
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            destination = session.createQueue(PUBLISH_QUEUE_NAME);
            producer = session.createProducer(destination);

            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                objectMessage = session.createObjectMessage(random.nextInt(100));
                producer.send(objectMessage);
                log.info("Message Sent Successfully");
            }
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
