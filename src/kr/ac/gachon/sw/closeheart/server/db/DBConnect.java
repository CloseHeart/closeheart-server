package kr.ac.gachon.sw.closeheart.server.db;

import java.sql.Connection;
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

}