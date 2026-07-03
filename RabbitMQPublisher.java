package Orchestrator.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.nio.charset.StandardCharsets;

public class RabbitMQPublisher {

    private static final String EXCHANGE_NAME = "orchestrator.events";

    public static void publish(String eventType, String json) {

        try (Connection connection = RabbitMQClient.createConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .contentType("application/json")
                    .deliveryMode(2)
                    .type(eventType)
                    .build();

            channel.basicPublish(EXCHANGE_NAME, "", props, json.getBytes(StandardCharsets.UTF_8));

            System.out.println("Published event: " + eventType);

        } catch (Exception e) {
            System.out.println("RabbitMQ publish failed: " + e.getMessage());
        }
    }
}
