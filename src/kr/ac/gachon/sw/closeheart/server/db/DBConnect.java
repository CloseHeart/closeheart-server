package kr.ac.gachon.sw.closeheart.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

public class DBConnect {
	/* 유저 생성 함수
	 * @author Minjae Seon
	 * @param email String 
	 * @param password String
	 * @param nickName String
	 * @return boolean
	 */
	public static boolean createUser(String email, String password, String nickName) {
		Connection dbConnection;
		try {
			dbConnection = DBManager.getDBConnection();
			HashMap<String, Object> newUser = new HashMap<String, Object>();
			newUser.put("user_mail", email);
			newUser.put("user_pw", password);
			newUser.put("user_nick", nickName);
			return DBManager.insertQuery(dbConnection, "account", newUser);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/* 로그인 함수
	 * @author Taehyun Park
	 * @param email String 
	 * @param password String
	 * @return boolean
	 */
	public static boolean loginMatchUser(String email, String password) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_mail");
			attrList.add("user_pw");
			
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_mail", email);
			conditionList.put("user_pw", password);
			
			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs.next()) {
				String user_email = rs.getString("user_mail");
				String user_pw = rs.getString("user_pw");
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}