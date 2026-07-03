package Orchestrator.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

public class RabbitMQSubscriber {

    private static final String EXCHANGE_NAME = "orchestrator.events";
    private static final String QUEUE_NAME = "orchestrator.events.queue";

    public static void main(String[] args) throws Exception {

        Connection connection = RabbitMQClient.createConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");

        System.out.println("Waiting for events... CTRL+C to stop.");

        DeliverCallback callback = (tag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            String type = delivery.getProperties().getType();
            System.out.println("Received event type=" + type + " body=" + message);
        };

        channel.basicConsume(QUEUE_NAME, true, callback, tag -> {});
    }
}
