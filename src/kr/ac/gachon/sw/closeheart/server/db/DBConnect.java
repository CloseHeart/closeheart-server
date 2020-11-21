package kr.ac.gachon.sw.closeheart.server.db;

import kr.ac.gachon.sw.closeheart.server.User;
import kr.ac.gachon.sw.closeheart.server.util.Util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

public class DBConnect {
	/* 유저 생성 함수
	 * @author Minjae Seon
	 * @param email 이메일 주소
	 * @param password 패스워드 (암호화)
	 * @param nickName 닉네임
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
	 * @param email 이메일 주소
	 * @param password 패스워드 (암호화)
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
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}