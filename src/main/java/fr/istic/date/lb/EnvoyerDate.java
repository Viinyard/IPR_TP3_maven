package fr.istic.date.lb;
/**
 * 
 */

import java.util.Date;

/**
 * @author VinYarD
 *
 * IPR_TP3//EmitLog.java
 * 6 févr. 2018
 */

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class EnvoyerDate {

  private static final String EXCHANGE_NAME = "date";

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri("amqp://mri:64GbL3k7uc33QCtc@localhost:8082/mri");
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();
    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);


    boolean loop = true;
    while(loop) {
    	channel.basicPublish(EXCHANGE_NAME, "date", null, EnvoyerDate.getDate().getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + getDate() + "'");
        Thread.sleep(1000);
    }
    

    channel.close();
    connection.close();
  }

  private static String getDate(){
    return (new Date()).toString();
  }
}