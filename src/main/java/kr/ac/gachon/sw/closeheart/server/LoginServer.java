package kr.ac.gachon.sw.closeheart.server;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.util.Util;

public class LoginServer extends Thread {
	private int port;
	public LoginServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		// Login Server 가동
		ExecutorService loginServerPool = Executors.newFixedThreadPool(1000);

		try (ServerSocket listener = new ServerSocket(port)) {
			// 서버 시작 알림 Print
			System.out.println("Login Server Starting....");
			while (true) {
				loginServerPool.execute(new loginServerHandler(listener.accept()));
			}
		} catch (Exception e) {
			System.out.println("Login Server Start Failed! - " + e.getMessage());
		}
	}

	/*
	 * Login Server Thread Handler
	 * @author Minjae Seon
	 */
	public static class loginServerHandler implements Runnable {
		private Socket socket;
		private Scanner in;
		private PrintWriter out;

		public loginServerHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Connected"));
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				while(in.hasNextLine()) {
					// Client가 보낸 JSON을 받아서 JsonObject 형태로 변환
					String clientRequest = in.nextLine();

					JsonObject clientJson = JsonParser.parseString(clientRequest).getAsJsonObject();

					/* 로그인 요청 처리 */
					// JSON이 Null이 아니고 요청 코드가 100이라면
					if(!clientJson.isJsonNull()) {
						int requestCode = clientJson.get("code").getAsInt();
						if(requestCode == 100) {
							if(loginHandler(clientJson)) {
								break;
							}
						}
						/* 아이디 중복 체크 처리*/
						else if(requestCode == 101) {
							checkIDHandler(clientJson);
						}
						/* 이메일 중복 체크 처리 */
						else if(requestCode == 102) {
							checkEmailHandler(clientJson);
						}
						/* 닉네임 중복 체크 처리 */
						else if(requestCode == 103) {
							checkNickHandler(clientJson);
						}
						/* 회원 가입 처리 */
						else if(requestCode == 104) {
							registerHandler(clientJson);
						}
						else if(requestCode == 105) {
							passwordResetHandler(clientJson);
						}
						/* 알 수 없는 Request Code 처리 */
						else {
							System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Not Valid Request!"));
							out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!"));
						}
					}
					// Json이 비어있는 Request라면 유효하지 않다고 보냄
					else {
						System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Not Valid Request!"));
						out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!"));
					}
				}
			} catch (Exception e) {
				if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error"));
				System.out.println("Login Server Error! " + e.getMessage());
			}
		}

		private boolean loginHandler(JsonObject clientJson) throws Exception {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Login Request"));
			// 함께 담긴 id / pw 값을 얻음
			String id = clientJson.get("id").getAsString();
			String pw = clientJson.get("pw").getAsString();

			// 로그인 토큰 생성
			String loginToken = Util.createAuthToken(id, pw);

			// LoginToken이 생성되었다면
			if (loginToken != null) {
				// 토큰 만료 시간 기록 (6시간)
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.HOUR_OF_DAY, 6);

				// 세션 정보를 DB에 기록, 생성 여부를 리턴받음
				boolean isSessionCreated = DBConnect.writeSession(id, loginToken, socket.getInetAddress().getHostAddress(), cal);

				// 세션이 잘 생성된 경우
				if (isSessionCreated) {
					System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Login Success! ID : " + id));
					// Login Token을 포함해 성공했다고 Client에 알림
					HashMap<String, Object> loginSuccessMap = new HashMap<>();
					loginSuccessMap.put("authToken", loginToken);
					loginSuccessMap.put("mainServerPort", String.valueOf(ServerMain.friendServerPort)); // 메인 (친구) 서버 포트를 알려줘 메인 서버로 연결을 유도
					String loginSuccessJson = Util.createJSON(200, loginSuccessMap);
					out.println(loginSuccessJson);
					out.close();
					in.close();
					socket.close();
					return true;
				}
				// 세선 생성 실패시
				else {
					System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Session Create Failed!"));
					out.println(Util.createSingleKeyValueJSON(500, "msg", "Session Create Failed"));
				}
			}
			// 생성 실패시 실패한 것을 Client에 알림
			else {
				System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Login Failed!"));
				out.println(Util.createSingleKeyValueJSON(401, "msg", "Login Failed"));
			}
			return false;
		}

		private void checkIDHandler(JsonObject clientJson) {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "ID Check Request"));

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

		private void checkEmailHandler(JsonObject clientJson) {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Email Check Request"));

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

		private void checkNickHandler(JsonObject clientJson) {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "NickName Check Request"));

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

		private void registerHandler(JsonObject clientJson) {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Register Request"));

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
					System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Register Success"));
					out.println(Util.createSingleKeyValueJSON(200, "msg", "Register Success"));
				}
				else {
					System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Register Failed!"));
					out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error"));
				}
			}
			else {
				out.println(Util.createSingleKeyValueJSON(403, "msg", "Duplicated"));
			}
		}

		private void passwordResetHandler(JsonObject clientJson) throws Exception {
			System.out.println(Util.createLogString("Login", socket.getInetAddress().getHostAddress(), "Password Reset Request"));
			String email = clientJson.get("email").getAsString();

			boolean checkEmail = DBConnect.emailCheck(email);

			if(checkEmail) {
				out.println(Util.createSingleKeyValueJSON(200, "msg", "success"));
			}
			else {
				out.println(Util.createSingleKeyValueJSON(400, "msg", "failed"));
			}
		}
	}
}
