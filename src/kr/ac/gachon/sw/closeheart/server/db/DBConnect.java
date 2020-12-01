package kr.ac.gachon.sw.closeheart.server.db;

import kr.ac.gachon.sw.closeheart.server.User;
import kr.ac.gachon.sw.closeheart.server.util.Util;

import java.sql.*;
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
	public static boolean createUser(String id, String email, String password, String nickName, String birthday) {
		Connection dbConnection;
		try {
			dbConnection = DBManager.getDBConnection();
			HashMap<String, Object> newUser = new HashMap<String, Object>();
			newUser.put("user_id", id);
			newUser.put("user_mail", email);
			newUser.put("user_pw", password);
			newUser.put("user_nick", nickName);
			newUser.put("user_birthday", birthday);

			return DBManager.insertQuery(dbConnection, "account", newUser);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/* 로그인 함수
	 * @author Taehyun Park
	 * @param id 아이디
	 * @param password 패스워드 (암호화)
	 * @return boolean
	 */
	public static boolean loginMatchUser(String id, String password) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");
			attrList.add("user_pw");
			
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", id);
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
	
	/* 토큰이용해 세션테이블에 액세스하는 함수
	 * @author Taehyun Park
	 * @param token 토큰
	 * @return ResultSet
	 */
	public static ResultSet AccessSessionWithToken(String token) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");
			
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("token", token);
			
			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "session", attrList, conditionList);
			if (rs.next()) {
				return rs;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/* 유저 아이디를 이용해 계정 테이블에 액세스하는 함수
	 * @author Taehyun Park
	 * @param user_id 유저 아이디
	 * @return ResultSet
	 */
	public static ResultSet AccessAccountWithId(String user_id) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_mail");
			attrList.add("user_pw");
			attrList.add("user_nick");
			attrList.add("user_birthday");
			attrList.add("user_statusmsg");
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", user_id);
			
			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs.next()) {
				return rs;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}

	/*
	 * 아이디 중복 확인 함수
	 * @author Minjae Seon
	 * @param id 아이디
	 * @return boolean
	 */
	public static boolean idCheck(String id) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", id);

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

	/*
	 * 이메일 중복 확인 함수
	 * @author Minjae Seon
	 * @param email 이메일 주소
	 * @return boolean
	 */
	public static boolean emailCheck(String email) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_mail");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_mail", email);

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

	/*
	 * 닉네임 중복 확인 함수
	 * @author Minjae Seon
	 * @param nick 닉네임
	 * @return boolean
	 */
	public static boolean nickCheck(String nick) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_nick");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_nick", nick);

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

	/*
	 * 유저 세션 정보 DB 입력
	 * @author Minjae Seon
	 * @param id 유저 ID
	 * @param token 유저 토큰
	 * @param timeInfo 만료 시간 정보를 담은 Calendar
	 * @return DB 쓰기 성공 여부
	 */
	public static boolean writeSession(String id, String token, String IP, Calendar expiredTimeInfo) {
		Connection dbConnection;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();

			// PreparedStatement 이용 Insert
			// Timestamp 기록을 위함
			PreparedStatement sessionStatement = dbConnection.prepareStatement("INSERT INTO session (user_id, token, clientIP, expiredTime) values (?, ?, ?, ?)");
			sessionStatement.setString(1, id); // ID
			sessionStatement.setString(2, token); // 토큰
			sessionStatement.setString(3, IP); // IP주소
			sessionStatement.setTimestamp(4, new Timestamp(expiredTimeInfo.getTimeInMillis())); // 만료시간
			// 전송
			int result = sessionStatement.executeUpdate();

			// 1개 이상의 결과가 있다면 true, 아니라면 false
			return result >= 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 토큰 유효성 검증
	 * @author Minjae Seon
	 * @param token 검증할 토큰 값
	 * @param clientIP 유저 IP 주소
	 * @return 유효 여부
	 */
	public static boolean isValidToken(String token, String clientIP) {
		Connection dbConnection;
		ResultSet rs = null;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");
			attrList.add("token");
			attrList.add("clientIP");
			attrList.add("expiredTime");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("token", token); // 토큰이 일치하고
			conditionList.put("clientIP", clientIP); // IP까지 일치해야 비로소 맞는 토큰 (동시 로그인을 지원하지만 IP 다르면 토큰 부정 사용임!)

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "session", attrList, conditionList);
			if (rs.next()) {
				Calendar cal = Calendar.getInstance();
				if(rs.getTimestamp("expiredTime").getTime() > cal.getTimeInMillis()) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * 토큰 삭제
	 * @author Minjae Seon
	 * @param token 삭제할 토큰 값
	 * @param clientIP 유저 IP 주소
	 * @return 성공 여부
	 */
	public static boolean removeToken(String token, String IP) {
		Connection dbConnection;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();

			PreparedStatement preparedStatement = dbConnection.prepareStatement("delete from session where token=? and clientIP=?");
			preparedStatement.setString(1, token);
			preparedStatement.setString(2, IP);

			int result = preparedStatement.executeUpdate();
			return result >= 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}