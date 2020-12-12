package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.object.User;
import kr.ac.gachon.sw.closeheart.server.util.Util;
import kr.ac.gachon.sw.closeheart.server.api.Covid19API;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.format.DateTimeFormatter;

public class FriendServer extends Thread {
    private int port;
    private HashMap<String, PrintWriter> userInfo = new HashMap<>();

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
    class friendServerHandler implements Runnable {
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

                    System.out.println(clientRequest);
                    JsonObject jsonObject = JsonParser.parseString(clientRequest).getAsJsonObject();
                    int requestCode = jsonObject.get("code").getAsInt();

                    // User가 null인 경우에는 User 정보를 먼저 받아와야 함
                    // Token을 이용해 user_id를 불러오고, 이를 이용해서 friend 테이블의 정보를 받아와야 함

                    if(user == null) {
                        /* Friend 서버 최초 접근시 Login 처리 */
                        if(requestCode == 300) {
                            if(!loginHandle(jsonObject)) {
                                break;
                            }
                            else {
                                userInfo.put(user.getUserID(), out);
                            }
                        }
                    }
                    // User 정보가 있을 경우 처리 -- 추후에
                    else {
                        // 토큰 유효성 체크해서 유효하지 않으면 명령 처리 단계 진입 불가
                        boolean isValidToken = DBConnect.isValidToken(user.getUserToken(), socket.getInetAddress().getHostAddress());
                        if(!isValidToken) {
                            out.println(Util.createSingleKeyValueJSON(403, "msg", "Token Not Valid!"));
                            in.close();
                            out.close();
                            socket.close();
                            break;
                        }

                        /* 친구 요청 */
                        if(requestCode == 302) {
                            // 요청 ID 가져옴
                            String friendRequestID = jsonObject.get("requestID").getAsString();
                            // 해당 아이디가 유효하다면
                            if(checkValidUser(friendRequestID)) {
                                if(!friendRequestID.equals(user.getUserID())) {
                                    // 친구 요청 전송
                                    try {
                                        boolean isSuccessFriendRequest = sendFriendRequest(friendRequestID);
                                        // 전송 성공하면 200, 실패하면 500
                                        if (isSuccessFriendRequest)
                                            out.println(Util.createSingleKeyValueJSON(200, "msg", "friendrequest"));
                                        else out.println(Util.createSingleKeyValueJSON(500, "msg", "friendrequest"));
                                    } catch (SQLIntegrityConstraintViolationException intgE) {
                                        // 제약 조건 에러 발생이면 이미 친구이거나 전송된 요청이므로 401
                                        out.println(Util.createSingleKeyValueJSON(401, "msg", "friendrequest"));
                                    }
                                }
                                else {
                                    // 본인에게 보낼 수 없는 요청 - 402
                                    out.println(Util.createSingleKeyValueJSON(402, "msg", "friendrequest"));
                                }
                            }
                            else {
                                out.println(Util.createSingleKeyValueJSON(400, "msg", "friendrequest"));
                            }
                        }
                    }

                    /* 로그아웃 처리 */
                    if(requestCode == 301) {
                        String userToken = jsonObject.get("token").getAsString();
                        boolean result = DBConnect.removeToken(userToken, socket.getInetAddress().getHostAddress());
                        out.println(Util.createSingleKeyValueJSON(301, "msg", "logout"));
                        System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Logout - Token Delete : " + result));
                    }

                    /* Covid-19 기능 처리*/
                    if(requestCode == 303){
                        LocalDate currentDate = LocalDate.now();
                        LocalDate ago1day = currentDate.minusDays(1);
                        String currentDateStr = currentDate.format(DateTimeFormatter.BASIC_ISO_DATE);
                        String agoDateStr = ago1day.format(DateTimeFormatter.BASIC_ISO_DATE);

                        JsonObject currentCovidInfo = Covid19API.getCovid19Data(currentDate);

                        try{
                            JsonObject currentObj = JsonParser.parseString(currentCovidInfo.toString()).getAsJsonObject();
                            JsonObject response = currentObj.get("response").getAsJsonObject();
                            JsonObject body = response.get("body").getAsJsonObject();
                            JsonObject items = body.get("items").getAsJsonObject();
                            JsonObject item = items.get("item").getAsJsonObject();

                            int currDecideCnd = item.get("decideCnt").getAsInt();

                            JsonObject agoOBj = JsonParser.parseString(agoCovidInfo.toString()).getAsJsonObject();
                            response = agoOBj.get("response").getAsJsonObject();
                            body = response.get("body").getAsJsonObject();
                            items = body.get("items").getAsJsonObject();
                            item = items.get("item").getAsJsonObject(); // 어제 확진자 수

                            int agoDecideCnd = item.get("decideCnt").getAsInt();
                            int newCnt = currDecideCnd - agoDecideCnd;  // 신규 확진자 수
                            //out.println("신규 확진자 = " + newCnt + ", 총 확진자 = " + currDecideCnd);
                            HashMap<String, String> covidInfo = new HashMap<>();
                            System.out.println(String.valueOf(newCnt));
                            covidInfo.put("newCnt", String.valueOf(newCnt));
                            covidInfo.put("currDecideCnd", String.valueOf(currDecideCnd));
                            out.println(Util.createJSON(303, covidInfo));
                            System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Covid-19 Date Send Success!"));
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            System.out.println("Conversion Failed!" + e.getMessage());
                            out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error") + "\n");
                        }

                    }
                }
            }
            catch (Exception e) {
                if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error") + "\n");
                System.out.println("Friend Server Error! " + e.getMessage());
            }
        }

        /*
         * User Login Handler
         * @author Taehyun Park, Minjae Seon
         * @param jsonObject Client로부터 받은 JsonObject
         * @throws Exception 에러 예외처리
         */
        private boolean loginHandle(JsonObject jsonObject) throws Exception {
            String user_id = null;
            String user_mail = null;
            String user_nick = null;
            String user_birthday = null;
            String user_statusmsg = null;
            String userToken = jsonObject.get("token").getAsString();

            ResultSet rs_session = DBConnect.AccessSessionWithToken(userToken);
            // 정상적으로 session테이블에 있는 user_id값 받아왔다면
            if(rs_session != null) {
                if (rs_session.next()) {
                    user_id = rs_session.getString(1);

                    // 얻어온 값으로 account 조회해서  모든 user info값 user객체에 저장
                    ResultSet rs_account = DBConnect.AccessAccountWithId(user_id);
                    if (rs_account != null) {
                        if (rs_account.next()) {
                            user_mail = rs_account.getString(1);
                            user_nick = rs_account.getString(2);
                            user_birthday = rs_account.getString(3);
                            user_statusmsg = rs_account.getString(4);
                        }

                        user = new User(userToken, user_id, user_nick, user_statusmsg, null);

                        // 서버로 유저 정보 전송
                        HashMap<String, String> userInfoMap = new HashMap<>();
                        userInfoMap.put("id", user_id);
                        userInfoMap.put("nick", user_nick);
                        userInfoMap.put("userMsg", user_statusmsg);
                        userInfoMap.put("friend", null);
                        out.println(Util.createJSON(200, userInfoMap));
                    }
                }
            }
            else {
                out.println(Util.createSingleKeyValueJSON(403, "msg", "Token Not Valid!"));
                in.close();
                out.close();
                socket.close();
                return false;
            }

            // 정보 로드 실패시
            if(user == null) {
                // 에러 발송 후 서버랑 연결 해제
                out.println(Util.createSingleKeyValueJSON(400, "msg", "Can't Find User Infomation!"));
                in.close();
                out.close();
                socket.close();
                return false;
            }
            return true;
        }

        /*
         * 유효한 유저인지 검증
         * @author Minjae Seon
         * @param requestID 요청 보낼 아이디
         * @throws Exception 에러 예외처리
         * @return Boolean
         */
        private boolean checkValidUser(String requestID) throws Exception {
            if (requestID.isEmpty()) return false;
            boolean isValidUser = DBConnect.isValidUser(requestID);
            if (isValidUser) return true;
            else return false;
        }

        /*
         * 친구 요청 전송
         * @author Minjae Seon
         * @param requestID 요청 보낼 아이디
         * @throws Exception 에러 예외처리
         * @return Boolean
         */
        private boolean sendFriendRequest(String requestID) throws Exception {
            if (requestID.isEmpty()) return false;
            boolean isSuccessRequest = DBConnect.requestFriend(user.getUserToken(), requestID);
            if(isSuccessRequest) return true;
            else return false;
        }
    }
}
