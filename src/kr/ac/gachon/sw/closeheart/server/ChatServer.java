  
package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.util.Util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends Thread {
	private int port;
	private static Set<PrintWriter> writers = new HashSet<>();	// 모든 writer

	private static HashMap<String, PrintWriter> mapOut = new HashMap<>();	// userToken + outStream
	private static HashMap<String, String> mapNic = new HashMap<>();	// userToekn + userNicName

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

					-- Server To Client --
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

					/*Chatting*/
					// JSON이 Null이 아니고
					if(!userJson.isJsonNull()){
						int requestCode = userJson.get("requestCode").getAsInt();
						/* 채팅방 입장 */
						if(requestCode == 210){
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Enter Chat Room Request"));
							// 함께 담긴 nicName과 token 얻음
							String userNic = userJson.get("nickName").getAsString();
							String userToken = userJson.get("token").getAsString();

							JsonObject toUser = new JsonObject();
							toUser.addProperty("type","join");
							toUser.addProperty("user",userNic);

							// 모든 친구들에게 JSONobj 전송
							for (PrintWriter writer : writers) {
								writer.println(toUser);
							}

							// hashset에 out추가
							writers.add(out)
							mapOut.put(userToken, out);	// hashmap에 Usertoken, out추가
							mapNic.put(userToken, userNic) // hashap에 UserToken, UserNic 추가

							if(!writers.contains(out) && mapNic.get(userToken) == null){
								System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Enter Chat Room Failed!"));
							}
							if(mapOut.get(userToken) == null) {}
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Enter Chat Room Success!"));

						}
						/* 채팅 메세지 전송 */
						else if(requestCode == 211){
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Msg Send Request"));
							// 함께 담긴 메세지와 token 얻음
							String userMsg = userJson.get("msg").getAsString();
							String userToken = userJson.get("token").getAsString();

							JsonObject toUser = new JsonObject();
							toUser.addProperty("type","message");
							toUser.addProperty("user",mapNic.get(userToken));
							toUser.addProperty("msg",userMsg);

							// 1:1 채팅 기능
							// 모든 친구들에게 JSONobj 전송
							for (PrintWriter writer : writers) {
								writer.println(toUser);
							}
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Msg Send Success!"));
						}
						/* 채팅방 나가기 */
						else if(requestCode == 212){
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Exit Chat Room Request"));
							// 함께 담긴 token 얻음
							String userToken = userJson.get("token").getAsString();

							JsonObject toUser = new JsonObject();
							toUser.addProperty("type","exit");
							toUser.addProperty("user",mapNic.get(userToken));

							writers.remove(out);	// HashSet에서 outStream 제거
							mapNic.remove(userToken);	// HashMap에서 해당 유저 제거
							mapOut.remove(userToken);

							if(writers.contains(out) && mapNic.get(userToken) != null){
								System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Exit Chat Room Failed!"));
							}
							if(mapOut.get(userToken) != null) {}

							// 모든 친구들에게 JSONobj 전송
							for (PrintWriter writer : writers) {
								writer.println(toUser);
							}
							try{
								socket.close();
							}
							catch(Exception e){
							}
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Exit Chat Room Success!"));
						}
						/* 알 수 없는 Request Code 처리 */
						else{	//
							System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Not Valid Request!"));
							out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!") + "\n");
						}
					}
					// Json이 비어있는 Request라면 유효하지 않다고 보냄
					else{
						System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Not Valid Request!"));
						out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!") + "\n");
					}
				}
			} catch (Exception e) {
				if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error"));
				System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Server Error - " + e.getMessage()));
			}
		}
	}
}