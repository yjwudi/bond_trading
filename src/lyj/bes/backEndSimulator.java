package lyj.bes;

import javax.jms.*;
import javax.jms.Message;

import org.apache.activemq.*;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;

import java.util.Vector;

import net.sf.json.*;

public class backEndSimulator {
	private static Vector<JSONObject> sellVec = new Vector<JSONObject>();
	private static Vector<JSONObject> buyVec = new Vector<JSONObject>();
	private static Vector<JSONObject> bidVec = new Vector<JSONObject>();
	private static Vector<JSONObject> offerVec = new Vector<JSONObject>();
	private static Session session;
	private static JSONObject obj;
	private static int lastID = 0;

	public static void main(String[] args) throws Exception {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(
				"tcp://localhost:61616");
		// ���ӹ�������������
		Connection connection = factory.createConnection();
		connection.start();
		// Session�� һ�����ͻ������Ϣ���߳�
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		// �洢��Ʊ������Ϣ��topic��queue
		Topic sellTopic = session.createTopic("sellTopic");
		Topic buyTopic = new ActiveMQTopic("buyTopic");
		Queue bidQueue = new ActiveMQQueue("bidQueue");
		Queue offerQueue = new ActiveMQQueue("offerQueue");
		// ������
		MessageConsumer simSellTopConsumer = session.createConsumer(sellTopic);
		MessageConsumer simBuyTopConsumer = session.createConsumer(buyTopic);
		MessageConsumer offerQueConsumer = session.createConsumer(offerQueue);
		MessageConsumer bidQueConsumer = session.createConsumer(bidQueue);

		// sell ��topic
		simSellTopConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				chekSellBond();
				TextMessage msg = (TextMessage) m;
				try {
					System.out.println(msg.getText());
					String ss = msg.getText();
					obj = JSONObject.fromObject(ss);
					long startTime = System.currentTimeMillis();
					obj.put("startTime", startTime);
					sellVec.addElement(obj);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		// buy ��topic
		simBuyTopConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					chekBuyBond();
					TextMessage msg = (TextMessage) m;
					System.out.println(msg.getText());
					obj = JSONObject.fromObject(msg.getText());
					long startTime = System.currentTimeMillis();
					obj.put("startTime", startTime);
					buyVec.addElement(obj);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
		// bid ��queue
		bidQueConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					// chekSellBond();
					TextMessage msg = (TextMessage) m;
					System.out.println(msg.getText());
					obj = JSONObject.fromObject(msg.getText());
					System.out.println(obj.getString("TradeID"));
					long startBidTime = System.currentTimeMillis();
					long tempBidTime;
					int tempID = Integer.valueOf(obj.getString("TradeID"));
					// System.out.println("lastID " + lastID+"tempID " +
					// tempID);
					if (lastID != tempID) {
						dealBid();
						bidVec.clear();
						lastID = tempID;
						startBidTime = System.currentTimeMillis();
					} else {
						tempBidTime = System.currentTimeMillis();
						if ((tempBidTime - startBidTime) < 20000) {
							bidVec.add(obj);
						}
					}
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});

		// offer ��Queue
		offerQueConsumer.setMessageListener(new MessageListener() {
			public void onMessage(Message m) {
				try {
					chekBuyBond();
					TextMessage msg = (TextMessage) m;
					obj = JSONObject.fromObject(msg.getText());
					dealOffer(obj);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ����5���ӵ�Buy��bond
	public static void chekBuyBond() {
		try {
			int i = 0;
			long startTime;
			long tmpTime = System.currentTimeMillis();
			obj = new JSONObject();
			String stpmsg;
			Topic stopBuyTopic = session.createTopic("stopBuyTopic");
			MessageProducer producer = session.createProducer(stopBuyTopic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			while (i != buyVec.size()) {
				for (i = 0; i < buyVec.size(); i++) {
					startTime = Long.parseLong(buyVec.get(i).getString(
							"startTime"));
					if ((tmpTime - startTime) > 300000) {
						obj = buyVec.get(i);
						stpmsg = "{Result:'CLOSED', TradeID:'"
								+ obj.getString("TradeID") + "'}";
						producer.send(session.createTextMessage(stpmsg));
						buyVec.remove(i);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ����5���ӵ�Sell��bond,����Closed���
	public static void chekSellBond() {

		try {
			int i = 0;
			long startTime;
			long tmpTime = System.currentTimeMillis();
			String stpmsg;
			Topic stopSellTopic = session.createTopic("stopSellTopic");
			MessageProducer producer = session.createProducer(stopSellTopic);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			while (i != sellVec.size()) {
				for (i = 0; i < sellVec.size(); i++) {
					startTime = Long.parseLong(sellVec.get(i).getString(
							"startTime"));
					if ((tmpTime - startTime) > 300000) {
						obj = sellVec.get(i);
						stpmsg = "{Result:'CLOSED', TradeID:'"
								+ obj.getString("TradeID") + "'}";
						producer.send(session.createTextMessage(stpmsg));
						sellVec.remove(i);
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// ����ÿ��BUY�����offer��Ϣ
	public static void dealOffer(JSONObject obj) {
		try {
			int i = 0;
			boolean flag = false;
			String resmsg;
			Queue offerResultQueue = new ActiveMQQueue("offerResultQueue");
			MessageProducer offerResultQueProducer = session
					.createProducer(offerResultQueue);
			int objID = Integer.valueOf(obj.getString("TradeID"));
			int tempID;
			for (i = 0; i < buyVec.size(); i++) {
				tempID = Integer.valueOf(buyVec.get(i).getString("tradeid"));
				if (tempID == objID) {
					flag = true;
					resmsg = "{Result:'EXECUTED', TradeID:'"
							+ obj.getString("TradeID") + "'}";
					offerResultQueProducer.send(session
							.createTextMessage(resmsg));
					buyVec.remove(i);
					break;
				}
			}
			if (!flag) {
				resmsg = "{Result:'REJECTED', TradeID:'"
						+ obj.getString("TradeID") + "'}";
				offerResultQueProducer.send(session.createTextMessage(resmsg));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ����ÿ��sell�����bid��Ϣ
	public static void dealBid() {
		if (bidVec.isEmpty()) {
			return;
		}
		try {
			int i = 0;
			Boolean flag = false;
			double maxPri = 0;
			String resmsg;
			Queue bidResultQueue = new ActiveMQQueue("bidResultQueue");
			MessageProducer bidResultProducer = session
					.createProducer(bidResultQueue);

			for (i = 0; i < bidVec.size(); i++) {
				double price = Double.valueOf(bidVec.get(i).getString("Price"))
						.doubleValue();
				if (maxPri < price) {
					maxPri = price;
					obj = bidVec.get(i);
					flag = true;
				}
			}

			if (flag) {
				resmsg = "{Result:'EXECUTED', TradeID:'"
						+ obj.getString("TradeID") + "'}";
			} else {
				resmsg = "{Result:'REJECTED', TradeID:'"
						+ obj.getString("TradeID") + "'}";
			}
			System.out.println(resmsg);
			bidResultProducer.send(session.createTextMessage(resmsg));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
