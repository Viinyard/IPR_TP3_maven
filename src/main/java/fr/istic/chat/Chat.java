package fr.istic.chat;

import com.rabbitmq.client.*;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * @author VinYarD
 * <p>
 * IPR_TP3//EmitLog.java
 * 6 févr. 2018
 */

public class Chat {

    private static final String EXCHANGE_NAME = "chat";

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        Options optionHelp = new Options();

        /*
            Help option group
         */
        {
            OptionGroup optionGroup = new OptionGroup();

            {
                Option option = new Option("h", "help", false, "print this message.");
                optionHelp.addOption(option);
            }

            {
                Option option = new Option("p", "pseudo", false, "Votre pseudo sur le chat");
                option.setType(String.class);
                option.setArgName("pseudo");
                option.setArgs(1);
                option.setRequired(true);
                options.addOption(option);
            }

            {
                Option option = new Option("t", "topic", true, "Topic auquel se connecter, exemple : chat.rmi");
                option.setType(String.class);
                option.setArgName("topic");
                option.setArgs(1);
                option.setRequired(true);
                options.addOption(option);
            }

            options.addOptionGroup(optionGroup);
        }


        /*
            Parse command
         */
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(optionHelp, args, true);

            if (cmd.hasOption("help")) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("-h | -p <pseudo> -t <topic>", options);
                System.exit(1);
            } else {
                cmd = parser.parse(options, args);

                String pseudo = cmd.getOptionValue("p");
                String topic = cmd.getOptionValue("t");

                /*
                    DEBUT CHAT
                 */
                ConnectionFactory factory = new ConnectionFactory();
                factory.setUri("amqp://mri:64GbL3k7uc33QCtc@localhost:8082/mri");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, EXCHANGE_NAME, topic);

                System.out.println("Welcome " + pseudo + " you are now connected to " + topic + " !");

                Consumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body) throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(envelope.getRoutingKey() + "#" + message);
                    }
                };
                channel.basicConsume(queueName, true, consumer);

                Scanner sc = new Scanner(System.in);
                String message;
                do {
                    message = sc.nextLine();

                    if (message.length() > 0) {
                        channel.basicPublish(EXCHANGE_NAME, topic, null, (pseudo + ">" + message).getBytes("UTF-8"));
                    }
                } while(!message.equals("exit"));

                sc.close();
                channel.close();
                connection.close();

                /*
                    FIN CHAT
                 */
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}