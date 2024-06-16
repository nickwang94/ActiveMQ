package com.nick.mq;

import com.nick.mq.consumer.TextConsumer;

public class ConsumerMainApplication {
    public static void main(String[] args) {
        TextConsumer textConsumer = new TextConsumer();
        textConsumer.receiveTextMessage();
    }
}