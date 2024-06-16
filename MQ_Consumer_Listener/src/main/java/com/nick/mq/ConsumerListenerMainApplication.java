package com.nick.mq;

import com.nick.mq.comsumer_listener.ConsumerListener;

public class ConsumerListenerMainApplication {
    public static void main(String[] args) {
        ConsumerListener consumerListener = new ConsumerListener();
        consumerListener.consumerMessage();
    }
}
