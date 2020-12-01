package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.util.Util;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendServer extends Thread {
    private int port;

    public FriendServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        // Friend Server 가동
        ExecutorService friendServerPool = Executors.newFixedThreadPool(10000);

        try (ServerSocket listener = new ServerSocket(port)) {
            // 서버 시작 알림 Print
            System.out.println("Friend Server Starting....");
            while (true) {
                friendServerPool.execute(new friendServerHandler(listener.accept()));
            }
        } catch (Exception e) {
            System.out.println("Friend Server Start Failed! - " + e.getMessage());
        }
    }

    /*
     * Friend Server Thread Handler
	 * @author Minjae Seon
	 */
    public static class friendServerHandler implements Runnable {
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private User user = null;

        public friendServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Connected"));
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (in.hasNextLine()) {
                    // Client가 보낸 JSON을 받아서 JsonObject 형태로 변환
                    String clientRequest = in.nextLine();
                    
                    // 비었으면 다음 줄 다시 받아오기
                    if(clientRequest.isEmpty()) clientRequest = in.nextLine();

                    JsonObject jsonObject = JsonParser.parseString(clientRequest).getAsJsonObject();
                    int requestCode = jsonObject.get("code").getAsInt();

                    // User가 null인 경우에는 User 정보를 먼저 받아와야 함
                    // Token을 이용해 user_id를 불러오고, 이를 이용해서 friend 테이블의 정보를 받아와야 함

                    if(user == null) {
                        /* Friend 서버 최초 접근시 Login 처리 */
                        if(requestCode == 300) {
                        	String user_id = null;
                        	String user_pw = null;
                        	String user_mail = null;
                        	String user_nick = null;
                        	String user_birthday = null;
                        	String user_statusmsg = null;
                            String userToken = jsonObject.get("token").getAsString();
                            ResultSet rs_session = DBConnect.AccessSessionWithToken(userToken);
                            // 정상적으로 session테이블에 있는 user_id값 받아왔다면
                            if(rs_session != null) {
                            	if (rs_session.next())
                    				user_id = rs_session.getString(1);
                            }
                            // 얻어온 값으로 account 조회해서  모든 user info값 user객체에 저장하는 것까지 일단 처리
                            ResultSet rs_account = DBConnect.AccessAccountWithId(user_id);
                            if(rs_account != null) {
                            	if (rs_account.next()) {
                            		user_mail = rs_account.getString(1);
	                            	user_pw = rs_account.getString(2);
	                            	user_nick = rs_account.getString(3);
	                            	user_birthday = rs_account.getString(4);
	                            	user_statusmsg = rs_account.getString(5);
                            	}
                            }
                            user = new User();
                        }
                    }
                    // User 정보가 있을 경우 처리
                    else {
                    	
                    }

                    /* 로그아웃 처리 */
                    if(requestCode == 301) {
                        String userToken = jsonObject.get("token").getAsString();
                        boolean result = DBConnect.removeToken(userToken, socket.getInetAddress().getHostAddress());
                        out.println(Util.createSingleKeyValueJSON(301, "msg", "logout"));
                        System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Logout - Token Delete : " + result));
                    }
                }
            }
            catch (Exception e) {
                if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error") + "\n");
                System.out.println("Friend Server Error! " + e.getMessage());
            }
        }
    }
}
