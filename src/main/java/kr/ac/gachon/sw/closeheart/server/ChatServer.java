
package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.object.User;
import kr.ac.gachon.sw.closeheart.server.util.Util;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer extends Thread {
    private int port;

    private static HashMap<String, ArrayList<Pair<String, PrintWriter>>> roomInfo = new HashMap<>(); // Room Info + User ID List
    private static HashMap<String, ArrayList<String>> roomPeopleInfo = new HashMap<>(); // Room Info + User Nick List

    public ChatServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
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

        private Scanner in;
        private PrintWriter out;
        private User myUser;

        public ChatHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            String currentRoomNumber = null;
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
                while (in.hasNextLine()) {
                    String userInput = in.nextLine();
                    if (userInput.isEmpty()) userInput = in.nextLine();

                    // 유저에게서 받은 JSON
                    JsonObject userJson = JsonParser.parseString(userInput).getAsJsonObject();

                    // 토큰 유효성 체크해서 유효하지 않으면 명령 처리 단계 진입 불가
                    boolean isValidToken = DBConnect.isValidToken(userJson.get("token").getAsString(), socket.getInetAddress().getHostAddress());
                    if (!isValidToken) {
                        out.println(Util.createSingleKeyValueJSON(403, "msg", "Token Not Valid!"));
                        break;
                    }

                    /*Chatting*/
                    // JSON이 Null이 아니고
                    if (!userJson.isJsonNull()) {
                        int requestCode = userJson.get("code").getAsInt();
                        /* 채팅방 입장 */
                        if (requestCode == 210) {
                            System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Enter Chat Room Request"));

                            // 유저 정보
                            myUser = DBConnect.AccessSessionWithToken(userJson.get("token").getAsString());
                            currentRoomNumber = userJson.get("roomNumber").getAsString();

                            // 방 없으면 생성
                            Pair<String, PrintWriter> pair = Pair.of(myUser.getUserID(), out);
                            if (!roomInfo.containsKey(currentRoomNumber)) {
                                ArrayList<Pair<String, PrintWriter>> userList = new ArrayList<>();
                                ArrayList<String> userNickList = new ArrayList<>();
                                userList.add(pair);
                                userNickList.add(myUser.getUserNick());
                                roomInfo.put(currentRoomNumber, userList);
                                roomPeopleInfo.put(currentRoomNumber, userNickList);
                            } else {
                                // 있으면 그냥 방에다 배정
                                roomInfo.get(currentRoomNumber).add(pair);
                                roomPeopleInfo.get(currentRoomNumber).add(myUser.getUserNick());
                            }

                            // 입장 메시지 전송
                            HashMap<String, Object> joinMap = new HashMap<>();
                            joinMap.put("type", "join");
                            joinMap.put("user", myUser.getUserNick());
                            joinMap.put("userlist", new Gson().toJsonTree(roomPeopleInfo.get(currentRoomNumber)));

                            ArrayList<Pair<String, PrintWriter>> currentUserList = roomInfo.get(currentRoomNumber);
                            for (Pair<String, PrintWriter> rpair : currentUserList) {
                                rpair.getValue().println(Util.createJSON(200, joinMap));
                            }
                            continue;
                        }
                        /* 채팅 메세지 전송 */
                        if (requestCode == 211) {
                            System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Msg Send Request"));
                            String userMsg = userJson.get("msg").getAsString();

                            // 메시지 보내기
                            HashMap<String, Object> msgMap = new HashMap<>();
                            msgMap.put("type", "message");
                            msgMap.put("user", myUser.getUserNick());
                            msgMap.put("msg", userMsg);

                            // 현재 방 번호 사람들에게 모두 전송
                            ArrayList<Pair<String, PrintWriter>> currentUserList = roomInfo.get(currentRoomNumber);
                            for (Pair<String, PrintWriter> pair : currentUserList) {
                                pair.getValue().println(Util.createJSON(200, msgMap));
                            }
                            System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Msg Send Success!"));
                        }
                        /* 채팅방 나가기 */
                        else if (requestCode == 212) {
                            System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Exit Chat Room Request"));
                            // 닉네임 목록에서 제거
                            roomPeopleInfo.get(currentRoomNumber).remove(myUser.getUserNick());

                            // 퇴장 알림
                            HashMap<String, Object> exitMap = new HashMap<>();
                            exitMap.put("type", "exit");
                            exitMap.put("user", myUser.getUserNick());
                            exitMap.put("userlist", new Gson().toJsonTree(roomPeopleInfo.get(currentRoomNumber)));

                            // 현재 방 번호 사람들에게 모두 전송
                            ArrayList<Pair<String, PrintWriter>> currentUserList = roomInfo.get(currentRoomNumber);
                            for (Pair<String, PrintWriter> pair : currentUserList) {
                                if (!pair.getKey().equals(myUser.getUserID())) {
                                    pair.getValue().println(Util.createJSON(200, exitMap));
                                }
                            }
                            break;
                        }
                        /* 알 수 없는 Request Code 처리 */
                        else {
                            System.out.println(Util.createLogString("Chat", socket.getInetAddress().getHostAddress(), "Not Valid Request!"));
                            out.println(Util.createSingleKeyValueJSON(400, "msg", "Not Valid Request!") + "\n");
                        }
                    } else {
                        // 이미 제거되었다면 break
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (currentRoomNumber != null) {
                ArrayList<Pair<String, PrintWriter>> currentUserList = roomInfo.get(currentRoomNumber);
                currentUserList.removeIf(pair -> pair.getKey().equals(myUser.getUserID()));
                roomInfo.put(currentRoomNumber, currentUserList);
            }

            try {
                in.close();
                out.close();
                socket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}