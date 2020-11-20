package kr.ac.gachon.sw.closeheart.server.util;

import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.JsonObject;

public class Util {
	/*
	 * Client에 전송할 Response JSON 생성
	 * @param responseCode 응답 코드
	 * @param elements 응답 내에 작성할 Property (속성)
	 * @return JSON 형태로 되어있는 String
	 */
	public static String createResponseJSON(int responseCode, HashMap<String, String> elements) {
		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("responseCode", responseCode);
		
		Iterator<String> keyIterator = elements.keySet().iterator();
		
		while(keyIterator.hasNext()) {
			String key = keyIterator.next();
			String value = elements.get(key);
			responseJSON.addProperty(key, value);
		}
		
		return responseJSON.getAsString();
	}
	
	/*
	 * Response Code와 단일 Key-Value만을 담은 Response JSON 생성
	 * @param responseCode 응답 코드
	 * @param key Key
	 * @param value Value
	 * @return JSON 형태로 되어있는 String
	 */
	public static String createSingleKeyValueJSON(int responseCode, String key, String value) {
		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("responseCode", responseCode);
		responseJSON.addProperty(key, value);
		return responseJSON.getAsString();
	}
}