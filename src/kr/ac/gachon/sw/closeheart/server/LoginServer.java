package kr.ac.gachon.sw.closeheart.server;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;

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
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);
				Gson gson = new Gson();
				
				// Client에 로그인 요청을 보냄
				HashMap<String, String> loginRequestHashMap = new HashMap<String, String>();
				loginRequestHashMap.put("msg", "Login Please");
				out.print(Util.createResponseJSON(401, loginRequestHashMap));
				
				Map<String, Object> requestMap = new HashMap<String, Object>();
				
				while(in.hasNext()) {
					// Client가 보낸 JSON을 받아서 Map 형태로 변환
					String clientRequest = in.nextLine();
					requestMap = (Map<String, Object>) gson.fromJson(clientRequest, requestMap.getClass());
					break;
				}
				
				// 여기서부터 계정 체크 해야함
					
			} catch (Exception e) {
				System.out.println("Login Server Error! " + e.getMessage());
			}
		}
	}
}
