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
				
				// Client에 로그인 요청을 보냄
				out.print(Util.createSingleKeyValueJSON(401, "msg", "Login Please"));

				while(in.hasNext()) {
					// Client가 보낸 JSON을 받아서 JsonArray 형태로 변환
					String clientRequest = in.nextLine();
					JsonArray clientJson = JsonParser.parseString(clientRequest).getAsJsonArray();
					
					if(!clientJson.isJsonNull() && clientJson.get(0).toString().equals("200")) {
						
					}
					else {
						out.print(Util.createSingleKeyValueJSON(500, "msg", "Not Valided Request!"));
					}
					
					break;
				}
				
				// 여기서부터 계정 체크 해야함
					
			} catch (Exception e) {
				System.out.println("Login Server Error! " + e.getMessage());
			}
		}
	}
}
