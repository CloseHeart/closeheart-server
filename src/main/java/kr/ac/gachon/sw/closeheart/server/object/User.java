package kr.ac.gachon.sw.closeheart.server.object;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class User {
	private String userToken;
	private String userID;
	private String userNick;
	private String userMsg;
	private String userEmail;
	private Date userBirthday;
	private Timestamp userLastTime;
	private ArrayList<User> friends;
	private boolean isOnline;

	// 토큰 포함 본인 유저 정보
	public User(String userToken, String userID, String userNick, String userMsg, String userEmail, Date userBirthday, ArrayList<User> friends) {
		this.userToken = userToken;
		this.userID = userID;
		this.userNick = userNick;
		this.userMsg = userMsg;
		this.userEmail = userEmail;
		this.userBirthday = userBirthday;
		this.friends = friends;
	}

	// 친구에 담을 유저 정보
	public User(String userID, String userNick, String userMsg, String userEmail, Date userBirthday, Timestamp userLastTime, boolean isOnline) {
		this.userID = userID;
		this.userNick = userNick;
		this.userMsg = userMsg;
		this.userEmail = userEmail;
		this.userBirthday = userBirthday;
		this.userLastTime = userLastTime;
		this.isOnline = isOnline;
		this.userEmail = userEmail;
		this.userBirthday = userBirthday;
		this.userLastTime = userLastTime;
	}

	public String getUserToken() {
		return userToken;
	}

	public String getUserID() {
		return userID;
	}

	public String getUserNick() {
		return userNick;
	}

	public String getUserMsg() {
		return userMsg;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public Date getUserBirthday() {
		return userBirthday;
	}

	public Timestamp getUserLastTime() {
		return userLastTime;
	}

	public ArrayList<User> getFriends() { return friends; }

	public boolean getOnline() {
		return isOnline;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public void setUserBirthday(Date userBirthday) {
		this.userBirthday = userBirthday;
	}

	public void setUserMsg(String userMsg) {
		this.userMsg = userMsg;
	}
	// 친구 유저 객체 정보를 현재 본인 유저의 친구 ArrayList에 추가
	public void setFriends(User friend) {
		if(friends == null) {
			friends = new ArrayList<>();
		}
		friends.add(friend);
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}