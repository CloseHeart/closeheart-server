package kr.ac.gachon.sw.closeheart.server.db;

import com.google.gson.Gson;
import kr.ac.gachon.sw.closeheart.server.object.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DBConnect {
	/* 유저 생성 함수
	 * @author Minjae Seon
	 * @param email 이메일 주소
	 * @param password 패스워드 (암호화)
	 * @param nickName 닉네임
	 * @return boolean
	 */
	public static boolean createUser(String id, String email, String password, String nickName, String birthday) {
		Connection dbConnection = null;
		try {
			dbConnection = DBManager.getDBConnection();
			// PreparedStatement 이용 Insert
			PreparedStatement sessionStatement = dbConnection.prepareStatement("INSERT INTO account (user_id, user_mail, user_pw, user_nick, user_birthday, user_lasttime) values (?, ?, ?, ?, ?, ?)");
			sessionStatement.setString(1, id);
			sessionStatement.setString(2, email);
			sessionStatement.setString(3, password);
			sessionStatement.setString(4, nickName);
			sessionStatement.setString(5, birthday);
			sessionStatement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

			// 전송
			int result = sessionStatement.executeUpdate();

			return result >= 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
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
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}
	
	/* 토큰 이용해 세션테이블에 액세스해 유저 정보 얻어오는 함수
	 * @author Taehyun Park, Minjae Seon
	 * @param token 토큰
	 * @return User
	 */
	public static User AccessSessionWithToken(String token) {
		Connection dbConnection = null;
		ResultSet rs = null;
		User user = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");
			attrList.add("expiredTime");
			
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("token", token);
			
			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "session", attrList, conditionList);

			if (rs.next()) {
				// 만료체크
				Timestamp expiredTime = rs.getTimestamp("expiredTime");
				if(expiredTime.getTime() < System.currentTimeMillis()) return null;

				String user_id = rs.getString(1);

				// 얻어온 값으로 account 조회해서  모든 user info값 user객체에 저장
				user = DBConnect.AccessAccountWithId(token, user_id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return user;
	}
	
	/* 유저 아이디를 이용해 계정 테이블에 액세스하는 함수
	 * @author Taehyun Park
	 * @param user_id 유저 아이디
	 * @return ResultSet
	 */
	public static User AccessAccountWithId(String token, String user_id) {
		Connection dbConnection = null;
		ResultSet rs = null;
		User user = null;
		try {
			dbConnection = DBManager.getDBConnection();
			
			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_mail");
			attrList.add("user_nick");
			attrList.add("user_birthday");
			attrList.add("user_statusmsg");
			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", user_id);
			
			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs != null) {
				if (rs.next()) {
					user = new User(token, user_id, rs.getString(2), rs.getString(4), rs.getString(1), rs.getDate(3), null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return user;
	}

	/* 본인 user_id이고 type이 0인 friend table의 행 얻어오는 함수
	 * @author Taehyun Park, Minjae Seon
	 * @param  user_id 유저 아이디
	 * @return ArrayList<User>
	 */
	public static ArrayList<User> AccessFriendTable(String user_id, int type) {
		Connection dbConnection = null;
		ResultSet rs = null;
		ArrayList<User> users = new ArrayList<>();
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user2_id");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user1_id", user_id);
			conditionList.put("type", type);

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "friend", attrList, conditionList);
			while (rs.next()) {
				// user의 친구 id 값 추출
				String userFriend_id = rs.getString(1);
				users.add(DBConnect.AccessAccountWithFriendId(userFriend_id));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return users;
	}

	/* 친구 유저 아이디 값을 이용해 계정 테이블에 액세스하는 함수
	 * @author Taehyun Park
	 * @param user_id 유저 아이디(친구 아이디값)
	 * @return User
	 */
	public static User AccessAccountWithFriendId(String user_id) {
		Connection dbConnection = null;
		ResultSet rs = null;
		User user = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_nick");
			attrList.add("user_statusmsg");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", user_id);

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs.next()) {
				user = new User(user_id, rs.getString(1), rs.getString(2), false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return user;
	}

	/* 존재하는 유저인지 체크
	 * @author Minjae Seon
	 * @param user_id 유저 아이디
	 * @return boolean
	 */
	public static boolean isValidUser(String userID) {
		Connection dbConnection = null;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_id");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_id", userID);

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}


	/*
	 * 아이디 중복 확인 함수
	 * @author Minjae Seon
	 * @param id 아이디
	 * @return boolean
	 */
	public static boolean idCheck(String id) {
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
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
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
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
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
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
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 유저 세션 정보 DB 입력
	 * @author Minjae Seon
	 * @param token 유저 토큰
	 * @param requestID 요청 받을 유저 ID
	 * @return DB 쓰기 성공 여부
	 */
	public static boolean requestFriend(String token, String requestID) throws Exception {
		// 토큰으로 아이디 가져오기
		User user = AccessSessionWithToken(token);
		String myID = user.getUserID();

		Connection dbConnection;
		// DB 연결 수립
		dbConnection = DBManager.getDBConnection();

		/*
			Friend Table Type 관련
			type 0 = 현재 친구인 상태
			type 1 = 내가 요청을 보낸 상태
			type 2 = 상대방에게 요청을 받은 상태
		*/
		PreparedStatement sessionStatement = dbConnection.prepareStatement("INSERT INTO friend (user1_id, user2_id, type) values (?, ?, ?)");
		sessionStatement.setString(1, myID); // 친구 요청을 보낸 유저 ID
		sessionStatement.setString(2, requestID); // 요청을 받을 유저 ID
		sessionStatement.setInt(3, 1); // 타입
		sessionStatement.addBatch();

		// 반대로도 저장
		sessionStatement.setString(1, requestID);
		sessionStatement.setString(2, myID);
		sessionStatement.setInt(3, 2);
		sessionStatement.addBatch();

		// 전송
		int[] result = sessionStatement.executeBatch();

		dbConnection.close();

		// 두 결과가 합쳐서 2 이상이면 true, 아니라면 false
		return result[0] + result[1] >= 2;
	}

	/*
	 * 토큰 유효성 검증
	 * @author Minjae Seon
	 * @param token 검증할 토큰 값
	 * @param clientIP 유저 IP 주소
	 * @return 유효 여부
	 */
	public static boolean isValidToken(String token, String clientIP) {
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
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
		Connection dbConnection = null;
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
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 만료된 토큰 삭제
	 * @return 성공 여부
	 */
	public static boolean removeExpiredToken() {
		Connection dbConnection = null;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();

			// Delete SQL 작성
			PreparedStatement preparedStatement = dbConnection.prepareStatement("delete from session where expiredTime < ?");

			// 현재 Timestamp를 구함
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());

			// ? 자리에 현재 Timestamp 삽입
			preparedStatement.setTimestamp(1, timestamp);

			// SQL문 실행
			preparedStatement.executeUpdate();

			return true;
		} catch (Exception e) {
			e.printStackTrace();;
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 코로나 정보 저장
	 * @param date String 형태의 날짜 (00000000)
	 * @param decideCnt 총 확진자 수
	 * @return 성공 여부
	 */
	public static boolean setCovid19Info(String date, int decideCnt) {
		Connection dbConnection = null;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();

			// Insert Into SQL 작성
			PreparedStatement preparedStatement = dbConnection.prepareStatement("insert into covid19api values(?, ?)");

			// 정보 삽입
			preparedStatement.setString(1, date);
			preparedStatement.setInt(2, decideCnt);

			// SQL문 실행
			preparedStatement.executeUpdate();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 해당 날짜의 코로나 19 정보 받기
	 * @param date 날짜 (00000000)
	 * @return 확진자 수 (실패시 -1)
	 */
	public static int getCovid19Info(String date) {
		Connection dbConnection = null;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("decideCnt");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("date", date);

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "covid19api", attrList, conditionList);

			if (rs.next()) {
				// 확진자 수 반환
				int decideCnt = rs.getInt("decideCnt");
				return decideCnt;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return -1;
	}


	/*
	 * 친구 수락 처리
	 * @author Minjae Seon
	 * @param userID 수락하는 유저 ID
	 * @param targetUserID 상대 ID
	 * @return 성공 여부
	 */
	public static boolean requestAccept(String userID, String targetUserID) {
		Connection dbConnection = null;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();
			// Insert Into SQL 작성
			PreparedStatement preparedStatement = dbConnection.prepareStatement("update friend set type = 0 where (user1_id = ? and user2_id = ?) or (user1_id = ? and user2_id = ?)");

			// 조건문
			preparedStatement.setString(1, userID);
			preparedStatement.setString(2, targetUserID);
			preparedStatement.setString(3, targetUserID);
			preparedStatement.setString(4, userID);

			// SQL문 실행
			preparedStatement.executeUpdate();

			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 친구 관계 삭제
	 * @author Minjae Seon
	 * @param userID 유저 ID
	 * @param targetUserID 상대 ID
	 * @return 성공 여부
	 */
	public static boolean removeFriendRelationship(String userID, String targetUserID) {
		Connection dbConnection = null;
		try {
			// DB 연결 수립
			dbConnection = DBManager.getDBConnection();
			// Insert Into SQL 작성
			PreparedStatement preparedStatement = dbConnection.prepareStatement("delete from friend where (user1_id = ? and user2_id = ?) or (user1_id = ? and user2_id = ?)");

			// 조건문
			preparedStatement.setString(1, userID);
			preparedStatement.setString(2, targetUserID);
			preparedStatement.setString(3, targetUserID);
			preparedStatement.setString(4, userID);

			// SQL문 실행
			preparedStatement.executeUpdate();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 비밀번호 초기화
	 * @author Minjae Seon
	 * @param id 유저 ID
	 * @param newPassword 새 패스워드 (암호화)
	 * @return 성공 여부
	 */
	public static boolean resetPassword(String id, String newPassword) {
		Connection dbConnection = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// PreparedStatement 이용 Insert
			PreparedStatement sessionStatement = dbConnection.prepareStatement("UPDATE account set user_pw = ? where user_id = ?");
			sessionStatement.setString(1, newPassword);
			sessionStatement.setString(2, id);

			// 전송
			int result = sessionStatement.executeUpdate();

			return result >= 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}

	/*
	 * 비밀번호 찾기시 일치 정보 찾기
	 * @author Minjae Seon
	 * @param email 이메일
	 * @param id 아이디
	 * @param birthday 생년월일
	 * @return boolean
	 */
	public static boolean checkFindPWInfo(String email, String id, String birthday) {
		Connection dbConnection = null;
		ResultSet rs = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// 가져올 Attribute List
			ArrayList<String> attrList = new ArrayList<String>();
			attrList.add("user_mail");

			// Condition HashMap
			HashMap<String, Object> conditionList = new HashMap<String, Object>();
			conditionList.put("user_mail", email);
			conditionList.put("user_id", id);
			conditionList.put("user_birthday", birthday);

			// SQL Select Query 전송
			rs = DBManager.selectQuery(dbConnection, "account", attrList, conditionList);
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}
	public static boolean resetNickname(String id, String newNickname) {
		Connection dbConnection = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// PreparedStatement 이용 Insert

			PreparedStatement sessionStatement = dbConnection.prepareStatement("UPDATE account set user_nick = ? where user_id = ?");
			sessionStatement.setString(1, newNickname);
			sessionStatement.setString(2, id);

			// 전송
			int result = sessionStatement.executeUpdate();

			return result >= 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}
	public static boolean resetStatusmsg(String id, String newStatusmsg) {
		Connection dbConnection = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// PreparedStatement 이용 Insert

			PreparedStatement sessionStatement = dbConnection.prepareStatement("UPDATE account set user_statusmsg = ? where user_id = ?");
			sessionStatement.setString(1, newStatusmsg);
			sessionStatement.setString(2, id);

			// 전송
			int result = sessionStatement.executeUpdate();

			return result >= 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}
	public static boolean resetBirthday(String id, String newBirthday) {
		Connection dbConnection = null;
		try {
			dbConnection = DBManager.getDBConnection();

			// PreparedStatement 이용 Insert

			PreparedStatement sessionStatement = dbConnection.prepareStatement("UPDATE account set user_birthday = ? where user_id = ?");
			sessionStatement.setString(1, newBirthday);
			sessionStatement.setString(2, id);

			// 전송
			int result = sessionStatement.executeUpdate();

			return result >= 1;

		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException throwables) {
					throwables.printStackTrace();
				}
			}
		}
		return false;
	}
}