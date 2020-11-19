package kr.ac.gachon.sw.closeheart.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

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
	
	/* Insert Into 함수
	 * @author Minjae Seon
	 * @param Connection Connection
	 * @param tableName tableName
	 * @param elements HashMap<String, String>
	 * @return Boolean
	 * @throw SQLException
	 */
	public static boolean insertQuery(Connection connection, String tableName, HashMap<String, String> elements) throws SQLException {
		// insert into TABLE_NAME (Attribute 1, Attribute 2..) VALUES (Value 1, Value 2..)
		Statement sm = connection.createStatement();
		
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("insert into " + tableName + " (");
		
		Iterator<String> keySetIterator = elements.keySet().iterator();
		
		// Key Set 전체 반복
		while (keySetIterator.hasNext()) {
			// Next 값 가져옴
			String key = keySetIterator.next();
			
			// 값을 추가
			strBuilder.append(key);
			
			// 다음 값이 있으면 ,를 추가하고 없다면 )를 추가
			if(keySetIterator.hasNext()) strBuilder.append(", ");
			else strBuilder.append(") VALUES (");
		}
		
		keySetIterator = elements.keySet().iterator();
		
		while(keySetIterator.hasNext()) {
			// Next Key 값 가져옴
			String key = keySetIterator.next();
			
			// Value 값 가져옴
			String value = elements.get(key);
			
			strBuilder.append("\"" + value + "\"");
			
			// 다음 값이 있으면 ,를 추가하고 없다면 )를 추가
			if(keySetIterator.hasNext()) strBuilder.append(", ");
			else strBuilder.append(");");
		}

		return sm.execute(strBuilder.toString());
	}
	
	public static ResultSet selectQuery(Connection connection, String tableName, String userMail, String userPw) throws SQLException {
		Statement sm = connection.createStatement();
		
		StringBuilder strBuilder = new StringBuilder();
		//strBuilder.append("select exists(select * from " + tableName + "where user_mail = " + userMail + " and user_pw = " + userPw + ");");
		strBuilder.append("select user_mail,user_pw from " + tableName + " where user_mail = " + "\"" +  userMail + "\"" + " and user_pw = " + "\"" + userPw+ "\"");
		System.out.println(strBuilder.toString());
		return sm.executeQuery(strBuilder.toString());
	}
}
