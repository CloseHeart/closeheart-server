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
				
				HashMap<String, String> loginRequestHashMap = new HashMap<String, String>();
				loginRequestHashMap.put("msg", "Login Please");
				out.print(Util.createResponseJSON(401, loginRequestHashMap));

				String clientRequest = in.nextLine();
				Map<String, Object> requestMap = new HashMap<String, Object>();
				requestMap = (Map<String, Object>) gson.fromJson(clientRequest, requestMap.getClass());
				
			} catch (Exception e) {
				System.out.println("Login Server Error! " + e.getMessage());
			}
		}
	}
}
