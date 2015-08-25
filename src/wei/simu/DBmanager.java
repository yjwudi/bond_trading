
package wei.simu;

import java.sql.Connection;
import java.sql.*;
import com.mysql.jdbc.Driver;

/**
 * version 1.0
 * @author wei
 *
 */
public class DBmanager {
	private static String url = "jdbc:mysql://localhost:3306/test";
	private static String username = "root";
	private static String password = "152374";
   /**
    * 数据库连接
    * @return
    * @throws SQLException
    * @throws IllegalAccessException
    * @throws ClassNotFoundException
    */
	public static Connection getConnection() throws SQLException,
			IllegalAccessException, ClassNotFoundException {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			connection = DriverManager.getConnection(url, username, password);
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return connection;
	}
    /**
     * 数据库关闭连接
     * @param connection
     * @param state
     * @param resultSet
     * @throws SQLException
     */
	public static void closeAll(Connection connection, Statement state,
			ResultSet resultSet) throws SQLException {
		if (resultSet != null) {
			resultSet.close();
		}
		if (state != null) {
			state.close();
		}
		if (connection != null) {
			connection.close();
		}
	}

	public static void main(String[] args) throws SQLException,
			IllegalAccessException, ClassNotFoundException {
		DBmanager.getConnection();
	}
}