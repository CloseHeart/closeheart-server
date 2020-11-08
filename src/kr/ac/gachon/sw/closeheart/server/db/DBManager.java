package kr.ac.gachon.sw.closeheart.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DB를 관리할 수 있는 기본적인 함수들을 담고 있는 Class
 * @author Minjae Seon
 * @date 2020.11.08
 */
public class DBManager {
	/*
	 * DB Connection을 얻어오는 함수
	 * @author Minjae Seon
	 * @return Connection
	 * @throw ClassNotFoundException
	 * @throw SQLException
	 */
	public static Connection getDBConnection() throws ClassNotFoundException, SQLException {
		Class.forName(DBInfo.jdbcDriver);
		return DriverManager.getConnection(DBInfo.url, DBInfo.id, DBInfo.password);
	}
	
	/* DB에 Query를 전송하는 함수 (성공 여부 리턴)
	 * @author Minjae Seon
	 * @return Boolean
	 * @throw SQLException
	 */
	public static boolean sendQuery(Connection connection, String query) throws SQLException {
		Statement sm = connection.createStatement();
		return sm.execute(query);
	}
	
	/*
	 * DB에 Query를 전송하는 함수 (결과값 리턴)
	 * @author Minjae Seon
	 * @return ResultSet 
	 * @throw SQLException
	 */
	public static ResultSet sendQuery_result(Connection connection, String query) throws SQLException {
		Statement sm = connection.createStatement();
		return sm.executeQuery(query);
	}
}
