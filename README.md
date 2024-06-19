# Active MQ Demo Project

## Start Active MQ

### Windows
double click `D:\Applications\apache-activemq-5.16.7\bin\win64\activemq`

### Mac
1. open terminal, cd to active path
2. cd bin
3. `sh activemq start`

## GUI

[ActiveMQ Link for Admin](http://127.0.0.1:8161/admin/queues.jsp)

Default username and password are both `admin`

# How to Start

## Queue Publish Msg

1. Start `src/main/java/com/nick/mq/ProducerMainApplication.java`
2. Check [Queue](http://127.0.0.1:8161/admin/queues.jsp)
3. There should be one msg in `PUB.QUEUE.HUB.1.0` queue

## Queue Consume Msg

1. Start `src/main/java/com/nick/mq/ConsumerMainApplication.java`
2. Check [Queue](http://127.0.0.1:8161/admin/queues.jsp)
3. This message should has been dequeued.

## Pub & Sub

1. Goto `MQ_PUB_SUB`
2. Start Consumer `src/main/java/com/nick/mq/consumer/TopicConsumer.java`
3. Start Producer `src/main/java/com/nick/mq/producer/TopicProducer.java`
4. Goto [Topic](http://127.0.0.1:8161/admin/topics.jsp) to check the result