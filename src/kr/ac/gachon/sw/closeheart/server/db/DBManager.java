package kr.ac.gachon.sw.closeheart.server.db;

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
	
	/* Insert Into Query 함수
	 * @author Minjae Seon
	 * @param connection Connection
	 * @param tableName String
	 * @param elements HashMap<String, Object>
	 * @return Boolean
	 * @throw SQLException
	 */
	public static boolean insertQuery(Connection connection, String tableName, HashMap<String, Object> elements) throws SQLException {
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
			Object value = elements.get(key);
			
			strBuilder.append("\"" + value + "\"");
			
			// 다음 값이 있으면 ,를 추가하고 없다면 )를 추가
			if(keySetIterator.hasNext()) strBuilder.append(", ");
			else strBuilder.append(");");
		}

		int check = sm.executeUpdate(strBuilder.toString());
		if(check == 0) return false;
		else return true;
	}
	
	/* Select Query 함수
	 * @author Taehyun Park, Minjae Seon
	 * @param connection Connection 
	 * @param tableName String
	 * @param attributeName ArrayList<String>
	 * @param condition HashMap<String, Object>
	 * @return ResultSet
	 * @throw SQLException
	 */
	public static ResultSet selectQuery(Connection connection, String tableName, ArrayList<String> attributeName, HashMap<String, Object> condition) throws SQLException {
		Statement sm = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("select ");
		
		// Attribute Name이 Null이거나 비어있지 않은 경우
		if(attributeName != null && !attributeName.isEmpty()) {
			// Attribute를 가져와서 Query문 생성
			Iterator<String> attrIterator = attributeName.iterator();
			while(attrIterator.hasNext()) {
				strBuilder.append(attrIterator.next());
				
				if(attrIterator.hasNext()) strBuilder.append(", ");
				else strBuilder.append("");
			}
			
			// Table Name 삽입
			strBuilder.append(" from " + tableName);
			
			// Condition이 Null이거나 비어있지 않은 경우
			if(condition != null && !condition.isEmpty()) {
				// where 삽입
				strBuilder.append(" where ");
				
				// Condition HashMap을 가져와서 Query문 생성
				Iterator<String> conditionIterator = condition.keySet().iterator();
				while(conditionIterator.hasNext()) {
					String key = conditionIterator.next();
					Object value = condition.get(key);
					strBuilder.append(key + " = " + "\"" + value + "\"");
					
					if(conditionIterator.hasNext()) strBuilder.append(" and ");
					else strBuilder.append(";");
				}
			}
			// Condition이 비었다면 ;로 마무리
			else strBuilder.append(";");

			System.out.println(strBuilder.toString());

			// 만든 Query문을 Execute함
			return sm.executeQuery(strBuilder.toString());
		}
		// 만약 문제가 있다면 null을 리턴
		return null;
	}
}
