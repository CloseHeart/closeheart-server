package kr.ac.gachon.sw.closeheart.server.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;

public class DBConnect {
	public static boolean createUser(String email, String password, String nickName) {
		Connection dbConnection;
		try {
			dbConnection = DBManager.getDBConnection();
			HashMap<String, String> newUser = new HashMap<String, String>();
			newUser.put("user_mail", email);
			newUser.put("user_pw", password);
			newUser.put("user_nick", nickName);
			return DBManager.insertQuery(dbConnection, "account", newUser);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean loginMatchUser(String email, String password) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();
			rs = DBManager.selectQuery(dbConnection, "account", email, password);
			if (rs.next()) {
				String user_email = rs.getString("user_mail");
				String user_pw = rs.getString("user_pw");
				System.out.println("user_email : " + user_email + ", user_pw : " + user_pw );
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

}