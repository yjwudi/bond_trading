package wei.simu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.math.*;

/**
 * version 1.0
 * @author wei
 *
 */
public class DbDAO {
	/**
	 * Statement对象
	 */
	private Statement state = null;

	/**
	 * PreparedStatement对象
	 */
	private PreparedStatement preState = null;

	/**
	 * 结果集对象
	 */
	private ResultSet resultSet = null;

	/**
	 * 数据库连接类对象
	 */
	private Connection connection = null;

	/**
	 * 数据库连接的包装类对象
	 */
	private DBmanager dbManager = null;

	/**
	 * 从数据库中随机获取bond
	 * @return
	 * @throws SQLException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 */
	public Map<String, String> showMessage() throws SQLException,
			IllegalAccessException, ClassNotFoundException {
		connection = DBmanager.getConnection();
		String sqlStr = "select * from bond order by rand() limit 1";
		state = connection.createStatement();
		resultSet = state.executeQuery(sqlStr);
		resultSet.next();
		String name = resultSet.getString("bondname");
		String cusip = resultSet.getString("bondcusip");
		int fv = resultSet.getInt("fv");
		double couponRate = resultSet.getDouble("couponrate");
		double ytm = resultSet.getDouble("ytm");
		int maturity = resultSet.getInt("maturity");
		String rate = resultSet.getString("creditrating");
		String expiredTimestamp = resultSet.getString("expiredtime");
		String result = "";
		result += "name:'" + name + "'," + "cusip:'" + cusip + "'," + "fv:'"
				+ fv + "'," + "couponRate:'" + couponRate + "'," + "ytm:'"
				+ ytm + "'," + "maturity:'" + maturity + "'," + "rate:'" + rate
				+ "'," + "expiredTimestamp:'" + expiredTimestamp + "'";
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", result);
		int quantity = resultSet.getInt("quantity");
		map.put("2", Integer.toString(quantity));
		map.put("ytm", Double.toString(ytm));
		map.put("Couprate", Double.toString(couponRate));
		map.put("fv", Integer.toString(fv));
		map.put("maturity", Integer.toString(maturity));
		DBmanager.closeAll(connection, preState, resultSet);
		return map;

	}
   /**
    * 随机自动产生购买bond的信息
    * 并随机对每个bond信息模拟用户offer这个bond的行为，至于有几个offer信息，随机产生
    * @return
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    * @throws SQLException
    */
	public Map<String, String> generateBuyMessageAndOfferRequest()
			throws IllegalAccessException, ClassNotFoundException, SQLException {
		Map<String, String> map = showMessage();
		Map<String, String> resultMap = new HashMap<String, String>();
		int quantity = Integer.parseInt(map.get("2"));
		String result2 = map.get("1");
		double fv = Double.parseDouble(map.get("fv"));
		double couprate = Double.parseDouble(map.get("Couprate"));
		double ytm = Double.parseDouble(map.get("ytm"));
		double maturity = Double.parseDouble(map.get("maturity"));
		double DivC = fv * couprate / 2.0;// c/2
		double DivR = ytm * 100.0 / 2.0;// r/2
		//计算bond基本价
		double MinPrice = DivC * (1 - Math.pow((1 + DivR), -2.0 * maturity))
				/ DivR + fv / Math.pow((1 + DivR), 2.0 * maturity);
		double price = Math.round(Math.random() * (fv - MinPrice)) + MinPrice;
		int tradeid = 100000 + (int)(Math.random() * 10);
		String result = "{action:'BUY',quantity:'" + quantity + "'," + result2
				+ ",tradeid:'"+Integer.toString(tradeid) + "',price:'" + price + "'}";
		resultMap.put("buyMessage", result);
		int offerNum = (int) (Math.random() * 10);
		resultMap.put("offerNum", Integer.toString(offerNum));
		String offerRequest = "{Account:'simulator',Action:'SELL',TradeID:'"+Integer.toString(tradeid)+"'}";
		for (int i = 0; i < offerNum; i++) {
			resultMap.put(Integer.toString(i), offerRequest);
		}
		return resultMap;

	}
    /**
     * 随机自动产生Sellbond的信息
     * 并随机对每个bond信息模拟用户bid这个bond的行为，至于有几个offer信息，随机产生，
     * 每个bid的价格也会根据算出来的基本价随机产生。
     * @return
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
	public Map<String, String> generateSellMessageAndBidRequest()
			throws IllegalAccessException, ClassNotFoundException, SQLException {

		Map<String, String> map = showMessage();
		Map<String, String> resultMap = new HashMap<String, String>();
		int quantity = Integer.parseInt(map.get("2"));
		String result2 = map.get("1");
		int tradeid = 100000 + (int)(Math.random() * 10);
		String result = "{action:'SELL',quantity:'" + quantity + "'," + result2
				+ ",tradeid:'"+Integer.toString(tradeid) + "'}";
		resultMap.put("sellMessage", result);
		double fv = Double.parseDouble(map.get("fv"));
		double couprate = Double.parseDouble(map.get("Couprate"));
		double ytm = Double.parseDouble(map.get("ytm"));
		double maturity = Double.parseDouble(map.get("maturity"));
		double DivC = fv * couprate / 2.0;// c/2
		double DivR = ytm * 100.0 / 2.0;// r/2
		double MinPrice = DivC * (1 - Math.pow((1 + DivR), -2.0 * maturity))
				/ DivR + fv / Math.pow((1 + DivR), 2.0 * maturity);

		int bibNum = (int) (Math.random() * 10);
		resultMap.put("bidNum", Integer.toString(bibNum));
		String bidRequest = "{Account:'simulator',Action:'BUY',TradeID:'"+Integer.toString(tradeid)+"',Price:'";
		for (int i = 0; i < bibNum; i++) {
			double price = Math.round(Math.random() * (fv - MinPrice))
					+ MinPrice;
			resultMap.put(Integer.toString(i), bidRequest + price + "'}");

		}
		return resultMap;

	}

}
