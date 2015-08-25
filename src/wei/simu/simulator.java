package wei.simu;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.region.Queue;
import org.apache.activemq.command.ActiveMQQueue;

import java.sql.*;

import com.mysql.jdbc.Driver;

import java.util.Map;

/**
 * version 1.0
 * @author wei
 *
 */

public class simulator {

	public static void main(String[] args) throws IllegalAccessException,
			ClassNotFoundException, SQLException {
		// ConnectionFactory �����ӹ�����JMS ������������
		ConnectionFactory connectionFactory;
		// Connection ��JMS �ͻ��˵�JMS Provider ������
		Connection connection = null;
		// Session�� һ�����ͻ������Ϣ���߳�
		Session session;
		// Destination ����Ϣ��Ŀ�ĵ�;��Ϣ���͸�˭.
		Destination sellDestination;
		Destination bidDestination;
		Destination bidResponseDestination;
		Destination buyDestination;
		Destination offerDestination;
		Destination offerResponsedestination;
		Destination stopSellDestination;
		Destination stopBuyDestination;
		Destination bidResultDestination;
		Destination offerResultDestination;

		// MessageProducer����Ϣ������
		MessageProducer sellProducer;
		MessageProducer bidProducer;
		MessageProducer buyProducer;
		MessageProducer offerProducer;
		// ������
		MessageConsumer stopSellConsumer;
		MessageConsumer stopBuyConsumer;
		MessageConsumer bidResultConsumer;
		MessageConsumer offerResultConsumer;

		// TextMessage message;
		// ����ConnectionFactoryʵ�����󣬴˴�����ActiveMq��ʵ��jar

		connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
		try {

			connection = connectionFactory.createConnection();
			// ����
			connection.start();
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			// ��ȡ��������
			// ����ӹ����õ����Ӷ���
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			//ע��һϵ�е�Topic��queue
			stopSellDestination = session.createTopic("stopSellTopic");
			stopSellConsumer = session.createConsumer(stopSellDestination);

			stopBuyDestination = session.createTopic("stopBuyTopic");
			stopBuyConsumer = session.createConsumer(stopBuyDestination);
			bidResultDestination = session.createQueue("bidResultQueue");
			bidResultConsumer = session.createConsumer(bidResultDestination);
            //���ü������õ���������սṹ���ǳɹ����Ǿܾ���
			stopSellConsumer.setMessageListener(new MessageListener() {
				public void onMessage(Message m) {
					try {
						System.out.println("simulator get "
								+ ((TextMessage) m).getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
				}

			});
			//��������queue��topic
			offerResultDestination = session.createQueue("offerResultQueue");
			offerResultConsumer = session
					.createConsumer(offerResultDestination);
				sellDestination = session.createTopic("sellTopic");
				buyDestination = session.createTopic("buyTopic");
				bidDestination = session.createQueue("bidQueue");
				offerDestination = session.createQueue("offerQueue");
				sellProducer = session.createProducer(sellDestination);
				// ���ò��־û�
				sellProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				// �õ���Ϣ�����ߡ������ߡ�
				buyProducer = session.createProducer(buyDestination);
				// ���ò��־û�
				buyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				bidProducer = session.createProducer(bidDestination);
				// ���ò��־û�
				bidProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				offerProducer = session.createProducer(offerDestination);
				// ���ò��־û�
				offerProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				while (true) {
				//�������õ�bid�����
				bidResultConsumer.setMessageListener(new MessageListener() {
					public void onMessage(Message m) {
						try {
							System.out.println("bid: simulator get "
									+ ((TextMessage) m).getText());
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}

				});
				//�������õ�offer���
				offerResultConsumer.setMessageListener(new MessageListener() {
					public void onMessage(Message m) {
						try {
							System.out.println("offer: simulator get "
									+ ((TextMessage) m).getText());
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				}); 
				//Sell֮������ս�����й㲥
				stopSellConsumer.setMessageListener(new MessageListener() {
					public void onMessage(Message m) {
						try {
							System.out.println("stopSell: simulator get "
									+ ((TextMessage) m).getText());
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}
				});
				//Buy���ս�����й㲥
				stopBuyConsumer.setMessageListener(new MessageListener() {
					public void onMessage(Message m) {
						try {
							System.out.println("stopBuy: simulator get "
									+ ((TextMessage) m).getText());
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}

				});
				sendSellMessage(session, sellProducer, bidProducer);
				sendBuyMessage(session, buyProducer, offerProducer);
				session.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != connection)
					connection.close();
			} catch (Throwable ignore) {
			}
		}
	}
	/**
	 * �Զ�����sell��Ϣ������ģ���û�bid��Ϣ���Զ�������ɣ�
	 * @param session
	 * @param sellProducer
	 * @param bidProducer
	 * @throws Exception
	 */
	public static void sendSellMessage(Session session,
			MessageProducer sellProducer, MessageProducer bidProducer)
			throws Exception {
		
		DbDAO db = new DbDAO();
		Map<String, String> map = db.generateSellMessageAndBidRequest();
		TextMessage sellMessage = session.createTextMessage(map
				.get("sellMessage"));
		System.out.println(map.get("sellMessage"));
		System.out.println(map.get("bidNum"));
		sellProducer.send(sellMessage);
		int bidnum = Integer.parseInt(map.get("bidNum"));
		for (int i = 0; i < bidnum; i++) {
			TextMessage bidMessage = session.createTextMessage(map.get(Integer
					.toString(i)));
			// ������Ϣ��Ŀ�ĵط�
			System.out.println(map.get(Integer.toString(i)));
			bidProducer.send(bidMessage);

			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		map.clear();

		try {
			Thread.sleep(6000 - bidnum * 100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
/**
 * �Զ�����sell��Ϣ������ģ���û�offer��Ϣ���Զ�������ɣ�
 * @param session
 * @param buyProducer
 * @param offerProducer
 * @throws Exception
 */
	public static void sendBuyMessage(Session session,
			MessageProducer buyProducer, MessageProducer offerProducer)
			throws Exception {
		DbDAO db = new DbDAO();
		Map<String, String> map = db.generateBuyMessageAndOfferRequest();
		TextMessage buyMessage = session.createTextMessage(map
				.get("buyMessage"));
		System.out.println(map.get("buyMessage"));
		System.out.println(map.get("offerNum"));
		buyProducer.send(buyMessage);
		int offernum = Integer.parseInt(map.get("offerNum"));
		for (int i = 0; i < offernum; i++) {
			//TextMessage offerMessage = session.createTextMessage(map
					//.get(Integer.toString(i)));
			TextMessage offerMessage = session.createTextMessage(map
					.get(Integer.toString(i)));
			// ������Ϣ��Ŀ�ĵط�
			System.out.println(map.get(Integer.toString(i)));
			offerProducer.send(offerMessage);

			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		map.clear();

		try {
			Thread.sleep(6000 - offernum * 100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}