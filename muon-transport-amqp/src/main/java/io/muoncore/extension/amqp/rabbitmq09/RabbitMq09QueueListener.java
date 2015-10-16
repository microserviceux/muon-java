package io.muoncore.extension.amqp.rabbitmq09;


import com.rabbitmq.client.*;
import io.muoncore.extension.amqp.QueueListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RabbitMq09QueueListener implements Runnable {

    private boolean running;
    private Channel channel;
    private Logger log = Logger.getLogger(RabbitMq09QueueListener.class.getName());
    private String queueName;
    private QueueListener.QueueFunction listener;
    private QueueingConsumer consumer;

    public RabbitMq09QueueListener(Channel channel, String queueName, QueueListener.QueueFunction function) {
        this.channel = channel;
        this.queueName = queueName;
        this.listener = function;
    }

    public void blockUntilReady() {
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {}
        }
    }

    @Override
    public void run() {
        try {
            log.info("Opening Queue: " + queueName);
            channel.queueDeclare(queueName, false, false, true, null);

            synchronized (this) {
                notify();
            }

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, false, consumer);
            log.info("Queue ready: " + queueName);

            running = true;
            while (running) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    byte[] content = delivery.getBody();

                    Map<String, Object> headers = delivery.getProperties().getHeaders();
                    if (headers == null) {
                        headers = new HashMap<>();
                    }
                    String contentType = "";
                    if (headers.get("Content-Type") != null) {
                        contentType = headers.get("Content-Type").toString();
                    }

                    listener.exec(new QueueListener.QueueMessage(queueName, content, headers, contentType));

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (ShutdownSignalException | ConsumerCancelledException ex) {
                    log.log(Level.FINER, ex.getMessage(), ex);
                } catch (Exception e) {
                    log.log(Level.WARNING, e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.log(Level.WARNING, e.getMessage(), e);
        }
        log.warning("Queue Listener exits: " + queueName);
    }

    public void cancel() {
        log.info("Queue listener is cancelled:" + queueName);
        running = false;
        try {
            consumer.handleCancel("Muon-Cancel");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                channel.queueDelete(queueName, false, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
