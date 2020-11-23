package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonArray;

public class User {
	private String userAuthToken;
	private String ID;
	private String userNickname;
	private JsonArray userFriends;

	public User(String ID, String userAuthToken, String userNickname) {
		this.ID = ID;
		this.userAuthToken = userAuthToken;
		this.userNickname = userNickname;
	}

	public String getUserToken() {
		return userAuthToken;
	}

	public String getUserNickname() {
		return userNickname;
	}

	public JsonArray getUserFriends() {
		return getUserFriends();
	}

	public void setUserFriends(JsonArray friendJsonArray) {
		this.userFriends = friendJsonArray;
	}
}
