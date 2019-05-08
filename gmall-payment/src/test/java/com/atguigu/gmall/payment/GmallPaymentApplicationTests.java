package com.atguigu.gmall.payment;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Test
	public void producer() {

		ConnectionFactory connect = new ActiveMQConnectionFactory("tcp://localhost:61616");
		try {
			Connection connection = connect.createConnection();
			connection.start();
			//第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
			Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
			Queue testqueue = session.createQueue("Boss Thirsty");

			MessageProducer producer = session.createProducer(testqueue);
			TextMessage textMessage=new ActiveMQTextMessage();
			textMessage.setText("把杯子装满水");
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.send(textMessage);
			session.commit();
			connection.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void condumer1(){
		ConnectionFactory connect = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,ActiveMQConnection.DEFAULT_PASSWORD,"tcp://localhost:61616");
		try {
			Connection connection = connect.createConnection();
			connection.start();
			//第一个值表示是否使用事务，如果选择true，第二个值相当于选择0
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination testqueue = session.createQueue("Boss Thirsty");

			MessageConsumer consumer = session.createConsumer(testqueue);
			consumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					if(message instanceof TextMessage){
						try {
							String text = ((TextMessage) message).getText();
							System.out.println(text);

							//session.rollback();
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();;
		}
	}
}

