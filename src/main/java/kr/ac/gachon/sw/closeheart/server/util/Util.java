package kr.ac.gachon.sw.closeheart.server.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import org.json.JSONArray;

public class Util {
	/*
	 * Client에 전송할 JSON 생성
	 * @param code 코드
	 * @param elements 응답 내에 작성할 Property (속성)
	 * @return JSON 형태로 되어있는 String
	 */
	public static String createJSON(int code, HashMap<String, Object> elements) {
		JsonObject json = new JsonObject();
		json.addProperty("code", code);

		Iterator<String> keyIterator = elements.keySet().iterator();

		while(keyIterator.hasNext()) {
			String key = keyIterator.next();
			Object value = elements.get(key);
			json.addProperty(key, String.valueOf(value));
		}

		return json.toString();
	}
	
	/*
	 * Code와 단일 Key-Value만을 담은 Response JSON 생성
	 * @param code 코드
	 * @param key Key
	 * @param value Value
	 * @return JSON 형태로 되어있는 String
	 */
	public static String createSingleKeyValueJSON(int code, String key, String value) {
		JsonObject json = new JsonObject();
		json.addProperty("code", code);
		json.addProperty(key, value);
		return json.toString();
	}

	/*
	 * SHA-256 암호화
	 * @author Minjae Seon
	 * @param originalString 원본 문자열
	 * @return SHA-256로 암호화된 문자열
	 */
	public static String encryptSHA256(String originalString) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] stringBytes = originalString.getBytes(StandardCharsets.UTF_8);
			messageDigest.update(stringBytes);
			return String.format("%064x", new BigInteger(1, messageDigest.digest()));
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}

	/*
	 * User의 인증 Token 생성
	 * @author Minjae Seon
	 * @param id 유저의 Email
	 * @param pw 유저의 Password
	 * @return 유저 Auth Token
	 */
	public static String createAuthToken(String id, String pw) {
		// 유저 정보 인증
		if(DBConnect.loginMatchUser(id, pw)) {
			// 안전을 위해 난수 생성을 통해 Token 복제를 방지함
			Random random = new Random();
			int randomInt = random.nextInt() + 1;

			// 또한 생성 시각도 삽입
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");

			// 이메일과 생성 시각, 난수를 통해 SHA256으로 감싸고 이를 유저를 인증하기 위한 토큰으로 사용함
			String token = id + dateFormat.format(cal.getTime()) + String.valueOf(randomInt);

			// 생성된 토큰 리턴
			return Util.encryptSHA256(token);
		}
		// 인증에 실패하면 null
		else return null;
	}

	/*
	 * 로그용 String 생성
	 * @author Minjae Seon
	 * @param tag 태그
	 * @param pw 유저 IP
	 * @param msg 메시지
	 * @return Log 문자
	 */
	public static String createLogString(String tag, String IP, String msg) {
		return "[" + tag + "][" + IP + "] " + msg;
	}

	/*
	 * SHA-512 암호화
	 * @author Minjae Seon
	 * @param originalString 원본 문자열
	 * @return SHA-512로 암호화된 문자열
	 */
	public static String encryptSHA512(String originalString) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			byte[] stringBytes = originalString.getBytes(StandardCharsets.UTF_8);
			messageDigest.update(stringBytes);
			return String.format("%0128x", new BigInteger(1, messageDigest.digest()));
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}
}