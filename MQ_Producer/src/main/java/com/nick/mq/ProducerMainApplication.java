package com.nick.mq;

import com.nick.mq.producer.ObjectProducer;
import com.nick.mq.producer.TextProducer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProducerMainApplication {

    public static void main(String[] args) {
        TextProducer textProducer = new TextProducer();
        ObjectProducer objectProducer = new ObjectProducer();
        objectProducer.sendObjectMessageInLoop();
    }
}