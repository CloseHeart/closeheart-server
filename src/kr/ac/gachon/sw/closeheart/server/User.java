package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class User {
	private String userAuthToken;
	private String userNickname;
	private JsonArray userFriends;

	public User(String userAuthToken, String userNickname) {
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
