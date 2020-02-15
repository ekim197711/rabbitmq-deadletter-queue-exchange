package com.example.rabbitmqdeadletterqueue;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RabbitMQSetupStuff {

    private static String QUEUE1 = "myqueue1";
    private static String DLQ1 = "dlq1";
    private static String EXCHANGE1 = "myexchange1";
    private static String DLX1 = "dlx1";
    private static String DLROUTINGKEY = "dlrk";
    private static String ROUTINGKEY = "myrk";
    static ConnectionFactory connectionFactory = null;
    public static ConnectionFactory getCF(){
        if (connectionFactory == null) {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            connectionFactory.setPort(5672);
            connectionFactory.setUsername("guest");
            connectionFactory.setPassword("guest");
        }
        return connectionFactory;

    }

    public static void main(String[] args) throws IOException, TimeoutException {

        Connection connection = getCF().newConnection();
        Channel channel = connection.createChannel();
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", 20000);
        arguments.put("x-dead-letter-exchange", DLX1);
        arguments.put("x-dead-letter-routing-key", DLROUTINGKEY + ".test1");

        channel.queueDelete(QUEUE1);
        channel.queueDeclare(QUEUE1, true, false, false,arguments);

        channel.exchangeDelete(EXCHANGE1);
        channel.exchangeDeclare(EXCHANGE1, "topic");
        channel.queueBind(QUEUE1, EXCHANGE1, ROUTINGKEY + ".#");

//        Dead Letter handling
        channel.queueDelete(DLQ1);
        channel.queueDeclare(DLQ1, true, false, false,new HashMap<>());

        channel.exchangeDelete(DLX1);
        channel.exchangeDeclare(DLX1, "topic");

        channel.queueBind(DLQ1,DLX1,DLROUTINGKEY + ".#");

        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties();
        channel.basicPublish(EXCHANGE1, ROUTINGKEY + ".mymessage1", basicProperties,
                "Hello this is a message created from Java! yaaaayy...".getBytes());

        connection.close();

    }
}
