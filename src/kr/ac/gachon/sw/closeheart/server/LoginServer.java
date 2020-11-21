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

					/* 로그인 요청 처리 */
					// JSON이 Null이 아니고 요청 코드가 100이라면
					if(!clientJson.isJsonNull()) {
						int requestCode = clientJson.get("requestCode").getAsInt();
						if(requestCode == 100) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Request");
							// 함께 담긴 id / pw 값을 얻음
							String id = clientJson.get("id").getAsString();
							String pw = clientJson.get("pw").getAsString();

							// 로그인 토큰 생성
							String loginToken = Util.createAuthToken(id, pw);

							// LoginToken이 생성되었다면
							if (loginToken != null) {
								System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Success! Login ID : " + id);
								// Login Token을 포함해 성공했다고 Client에 알림
								HashMap<String, String> loginSuccessMap = new HashMap<>();
								loginSuccessMap.put("authToken", loginToken);
								loginSuccessMap.put("mainServerPort", ""); // 메인 서버 포트를 알려줘 메인 서버로 연결을 유도
								String loginSuccessJson = Util.createResponseJSON(200, loginSuccessMap);
								out.println(loginSuccessJson);
								break;
							}
							// 생성 실패시 실패한 것을 Client에 알림
							else {
								System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Login Failed!");
								out.println(Util.createSingleKeyValueJSON(401, "msg", "Login Failed") + "\n");
							}
						}
						/* 아이디 중복 체크 처리*/
						else if(requestCode == 101) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] ID Check Request");

							// ID 부분을 가져옴
							String ID = clientJson.get("id").getAsString();

							// DB에서 중복 ID 체크
							boolean isRegistered = DBConnect.idCheck(ID);

							// 중복되는 값이 있으면
							if(isRegistered) {
								// 존재한다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "idcheck", "true"));
							}
							else {
								// 존재하지 않는다면 존재하지 않는다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "idcheck", "false"));
							}
						}
						/* 이메일 중복 체크 처리 */
						else if(requestCode == 102) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Email Check Request");

							// ID 부분을 가져옴
							String email = clientJson.get("email").getAsString();

							// DB에서 중복 ID 체크
							boolean isRegistered = DBConnect.emailCheck(email);

							// 중복되는 값이 있으면
							if(isRegistered) {
								// 존재한다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "emailcheck", "true"));
							}
							else {
								// 존재하지 않는다면 존재하지 않는다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "emailcheck", "false"));
							}

						}
						/* 닉네임 중복 체크 처리 */
						else if(requestCode == 103) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Nickname Check Request");

							// 닉네임 부분을 가져옴
							String nick = clientJson.get("nick").getAsString();

							// DB에서 중복 이메일 체크
							boolean isRegistered = DBConnect.nickCheck(nick);

							// 중복되는 값이 있으면
							if(isRegistered) {
								// 존재한다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "nickcheck", "true"));
							}
							else {
								// 존재하지 않는다면 존재하지 않는다고 Client에 알림
								out.println(Util.createSingleKeyValueJSON(200, "nickcheck", "false"));
							}
						}
						/* 회원 가입 처리 */
						else if(requestCode == 104) {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Register Request");

							// 가입 정보를 가져옴
							String id = clientJson.get("id").getAsString();
							String pw = clientJson.get("pw").getAsString();
							String email = clientJson.get("email").getAsString();
							String nick = clientJson.get("nick").getAsString();
							String birthday = clientJson.get("birthday").getAsString();

							// 혹시 모르니 아이디와 닉네임, 이메일 한 번 더 중복 체크
							boolean idCheck = DBConnect.idCheck(id);
							boolean nickCheck = DBConnect.nickCheck(nick);
							boolean emailCheck = DBConnect.emailCheck(email);
							if(!idCheck && !nickCheck && !emailCheck) {
								// 계정 생성
								boolean isCreated = DBConnect.createUser(id, email, pw, nick, birthday);

								// 생성되었으면 200 (정상), 실패했으면 서버 문제이므로 500
								if(isCreated) {
									System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Register Success");
									out.println(Util.createSingleKeyValueJSON(200, "msg", "Register Success") + "\n");
								}
								else {
									System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Register Failed!");
									out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error") + "\n");
								}
							}
							else {
								out.println(Util.createSingleKeyValueJSON(403, "msg", "Duplicated") + "\n");
							}
						}
						/* 알 수 없는 Request Code 처리 */
						else {
							System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Not Valid Request!");
							out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!") + "\n");
						}
					}
					// Json이 비어있는 Request라면 유효하지 않다고 보냄
					else {
						System.out.println("[" + socket.getInetAddress() + ":" + socket.getPort() + "] Not Valid Request!");
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
