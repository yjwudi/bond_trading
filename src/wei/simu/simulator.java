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
		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		ConnectionFactory connectionFactory;
		// Connection ：JMS 客户端到JMS Provider 的连接
		Connection connection = null;
		// Session： 一个发送或接收消息的线程
		Session session;
		// Destination ：消息的目的地;消息发送给谁.
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

		// MessageProducer：消息发送者
		MessageProducer sellProducer;
		MessageProducer bidProducer;
		MessageProducer buyProducer;
		MessageProducer offerProducer;
		// 消费者
		MessageConsumer stopSellConsumer;
		MessageConsumer stopBuyConsumer;
		MessageConsumer bidResultConsumer;
		MessageConsumer offerResultConsumer;

		// TextMessage message;
		// 构造ConnectionFactory实例对象，此处采用ActiveMq的实现jar

		connectionFactory = new ActiveMQConnectionFactory(
				ActiveMQConnection.DEFAULT_USER,
				ActiveMQConnection.DEFAULT_PASSWORD, "tcp://localhost:61616");
		try {

			connection = connectionFactory.createConnection();
			// 启动
			connection.start();
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			// 获取操作连接
			// 构造从工厂得到连接对象
			session = connection.createSession(Boolean.TRUE,
					Session.AUTO_ACKNOWLEDGE);
			//注册一系列的Topic和queue
			stopSellDestination = session.createTopic("stopSellTopic");
			stopSellConsumer = session.createConsumer(stopSellDestination);

			stopBuyDestination = session.createTopic("stopBuyTopic");
			stopBuyConsumer = session.createConsumer(stopBuyDestination);
			bidResultDestination = session.createQueue("bidResultQueue");
			bidResultConsumer = session.createConsumer(bidResultDestination);
            //设置监听，得到购买的最终结构（是成功还是拒绝）
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
			//创建各种queue和topic
			offerResultDestination = session.createQueue("offerResultQueue");
			offerResultConsumer = session
					.createConsumer(offerResultDestination);
				sellDestination = session.createTopic("sellTopic");
				buyDestination = session.createTopic("buyTopic");
				bidDestination = session.createQueue("bidQueue");
				offerDestination = session.createQueue("offerQueue");
				sellProducer = session.createProducer(sellDestination);
				// 设置不持久化
				sellProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				// 得到消息生成者【发送者】
				buyProducer = session.createProducer(buyDestination);
				// 设置不持久化
				buyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

				bidProducer = session.createProducer(bidDestination);
				// 设置不持久化
				bidProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				offerProducer = session.createProducer(offerDestination);
				// 设置不持久化
				offerProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				while (true) {
				//监听，得到bid结果。
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
				//监听，得到offer结果
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
				//Sell之后的最终结果进行广播
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
				//Buy最终结果进行广播
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
	 * 自懂发送sell消息并附带模拟用户bid消息（自动随机生成）
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
			// 发送消息到目的地方
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
 * 自动发送sell消息并附带模拟用户offer消息（自动随机生成）
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
			// 发送消息到目的地方
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