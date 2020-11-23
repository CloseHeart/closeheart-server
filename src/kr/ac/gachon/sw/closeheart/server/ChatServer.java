  
package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.util.Util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends Thread {
	private int port;

	private static Set<String> names = new HashSet<>();
	private static Set<PrintWriter> writers = new HashSet<>();

	private static int memberCnt = 1;	// 순서 대로 들어온 클라이언트끼리 대화 하도록, 클라이언트의 숫자를 카운트

	private static HashMap<Integer, PrintWriter> map = new HashMap<>();	// 클라이언트 + outStream
	private static HashMap<Integer, Boolean> isOnline = new HashMap<>();	// 클라이언트 + state

	public ChatServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
			/*
			System.out.println("The chat server is running...");
		Scanner keyborad = new Scanner(System.in);
		ServerSocket servercocket = null;
		Socket socekt = null;
		String fileName = "input.txt";

		try{
			servercocket = new ServerSocket(7777);
			System.out.println("Waiting for client..");

			socekt = servercocket.accept();
			System.out.println("Connected with client.");
			FileSender fs = new FileSender(socekt, fileName);
			fs.start();

		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			System.out.println("End of ChatServer");
		}
		 */

		// Chat Server Thread 시작
		ExecutorService pool = Executors.newFixedThreadPool(1000);
		try (ServerSocket listener = new ServerSocket(port)) {
			System.out.println("Chat Server Starting....");
			while (true) {
				pool.execute(new ChatHandler(listener.accept()));
			}
		} catch (Exception e) {
			System.out.println("Chat Server Start Failed! - " + e.getMessage());
		}
	}

	private static class ChatHandler implements Runnable {
		private Socket socket;
		private String nickName;

		private Scanner in;
		private PrintWriter out;

		public ChatHandler(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				/*
					Chatting Protocol

					-- Client To Server --
					requestCode 210 - 채팅방 입장
					 - 함께 첨부 될 내용 : 토큰, 닉네임 (nickName String에 저장하시면 됩니다.)
					 Example JSON - {requestCode:210, token:"토큰", nickName:"닉네임"}
					requestCode 211 - 채팅 메시지 전송
					 - 함께 첨부 될 내용 : 토큰, 메시지 내용
					 Example JSON - {requestCode:211, token:"토큰", msg:"하이"}
					requestCode 212 - 채팅방 나가기
					 - 함께 첨부 될 내용 - 토큰
					 Example JSON - {requestCode:212, token:"토큰"}

					클라이언트의 입장 요청을 받고 처리하는 방법 (requestCode 210)
					 - type에는 join, user에는 들어온 유저 닉네임을 담고 모든 유저에게 전송
					 Example JSON - {"type":"join", "user":"들어온 유저 닉네임"}

					클라이언트의 채팅 메시지를 받고 모든 유저에게 채팅 메시지를 전송하는 방법 (requestCode 211)
					 - type에는 message, user에는 전송 유저 닉네임, msg에는 Client에게서 넘어오는 메시지를 담고 모든 유저에게 전송
					 Example JSON - {"type":"message", "user":"전송 유저 닉네임", "msg":"메시지 내용"}

					클라이언트의 퇴장 요청을 받고 처리하는 방법 (requestCode 212)
					 - type에는 exit, user에는 나간 유저 닉네임을 담고 모든 유저에게 전송
					 Example JSON - {"type":"exit", "user":"나간 유저 닉네임"}

					 토큰 관련 처리는 나중에 다른 Class와 함께 추가 예정 - 일단은 채팅 주고받을 수 있도록 구현이 우선!
				 */

				// 유저 인풋 처리
				while(in.hasNextLine()) {
					String userInput = in.nextLine();
					if(userInput.isEmpty()) userInput = in.nextLine();

					// 유저에게서 받은 JSON
					JsonObject userJson = JsonParser.parseString(userInput).getAsJsonObject();
				}


			} catch (Exception e) {
				if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error"));
				System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Server Error - " + e.getMessage()));
			}
		}
	}
}