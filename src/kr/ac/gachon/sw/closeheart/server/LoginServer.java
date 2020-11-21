package kr.ac.gachon.sw.closeheart.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.util.Util;

public class LoginServer {
	/*
	 * Login Server Thread Handler
	 * @author Minjae Seon
	 * @description Login 처리를 할 Thread Handler - Login Server는 유저의 입력만을 처리하면 되므로 I/O만 잘 처리하면 됨
	 */
	public static class loginServerHandler implements Runnable {
		private Socket socket;
		private Scanner in;
		private PrintWriter out;
		private User user;

		public loginServerHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Connected");
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				while(in.hasNextLine()) {
					// Client가 보낸 JSON을 받아서 JsonObject 형태로 변환
					String clientRequest = in.nextLine();

					JsonObject clientJson = JsonParser.parseString(clientRequest).getAsJsonObject();

					// JSON이 Null이 아니고 요청 코드가 100이라면
					if(!clientJson.isJsonNull() && clientJson.get("requestCode").getAsInt() == 100) {
						System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Request");
						// 함께 담긴 id / pw 값을 얻음
						String id = clientJson.get("id").getAsString();
						String pw = clientJson.get("pw").getAsString();

						// 로그인 토큰 생성
						String loginToken = Util.createAuthToken(id, pw);

						// LoginToken이 생성되었다면
						if(loginToken != null) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Success ID :" + id);
							// Login Token을 포함해 성공했다고 Client에 알림
							HashMap<String, String> loginSuccessMap = new HashMap<>();
							loginSuccessMap.put("authToken", loginToken);
							String loginSuccessJson = Util.createResponseJSON(200, loginSuccessMap);
							out.println(loginSuccessJson);
							break;
						}
						// 생성 실패시 실패한 것을 Client에 알림
						else {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Failed!");
							out.println(Util.createSingleKeyValueJSON(403, "msg", "Login Failed") + "\n");
						}
					}
					// 이상한 Request라면 유효하지 않다고 보냄
					else {
						System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Not Valid Request.");
						out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!") + "\n");
					}
				}
			} catch (Exception e) {
				out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error") + "\n");
				System.out.println("Login Server Error! " + e.getMessage());
			}
		}
	}
}
