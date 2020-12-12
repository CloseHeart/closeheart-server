package kr.ac.gachon.sw.closeheart.server.object;

import java.util.ArrayList;

public class User {
	private String userToken;
	private String userID;
	private String userNick;
	private String userMsg;
	private ArrayList<User> friends;
	private boolean isOnline;

	// 토큰 포함 본인 유저 정보
	public User(String userToken, String userID, String userNick, String userMsg, ArrayList<User> friends) {
		this.userToken = userToken;
		this.userID = userID;
		this.userNick = userNick;
		this.userMsg = userMsg;
		this.friends = friends;
	}

	// 친구에 담을 유저 정보
	public User(String userID, String userNick, String userMsg, boolean isOnline) {
		this.userID = userID;
		this.userNick = userNick;
		this.userMsg = userMsg;
		this.isOnline = isOnline;
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

	public ArrayList<User> getFriends() { return friends; }

	public boolean getOnline() {
		return isOnline;
	}

	public void setUserMsg(String userMsg) {
		this.userMsg = userMsg;
	}

	public void setFriends(ArrayList<User> friends) { this.friends = friends; }

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
	}
}
