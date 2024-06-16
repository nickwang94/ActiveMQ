package com.nick.mq.comsumer_listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * 使用监听器的方式来处理消息
 */
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
