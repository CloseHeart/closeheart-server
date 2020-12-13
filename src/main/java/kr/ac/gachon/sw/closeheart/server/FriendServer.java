package kr.ac.gachon.sw.closeheart.server;

import com.google.gson.*;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import kr.ac.gachon.sw.closeheart.server.object.User;
import kr.ac.gachon.sw.closeheart.server.util.Util;
import kr.ac.gachon.sw.closeheart.server.api.Covid19API;
import org.json.JSONObject;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.time.LocalTime;
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
                            if(!loginHandler(jsonObject)) {
                                break;
                            }
                            else {
                                userInfo.put(user.getUserID(), out);
                            }
                        }
                        // 이외 코드는 전부 다 인증 후에만 처리해야함
                        else {
                            // 403 전송
                            out.println(Util.createSingleKeyValueJSON(403, "msg", "Not Authorization"));
                        }
                    }
                    // User 정보가 있을 경우 처리
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
                                        if (isSuccessFriendRequest) {
                                            out.println(Util.createSingleKeyValueJSON(200, "msg", "friendrequest"));

                                            /* 친구 온라인이면 여기서 요청 메시지 전송하기 ! */

                                            if(userInfo.containsKey(friendRequestID)) {
                                                HashMap<String, Object> friendRequestSendMap = new HashMap<>();
                                                friendRequestSendMap.put("msg", "friendreceive");
                                                friendRequestSendMap.put("userID", user.getUserID());
                                                friendRequestSendMap.put("userNick", user.getUserNick());
                                                userInfo.get(friendRequestID).println(Util.createJSON(200, friendRequestSendMap));
                                            }
                                        }
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
                        /* Covid-19 기능 처리*/
                        else if(requestCode == 303) {
                            try {
                                LocalDate currentDate = LocalDate.now();
                                LocalTime currentTime = LocalTime.now();
                                if(currentTime.getHour() < 11 && currentTime.getMinute() < 30) currentDate =  currentDate.minusDays(1); // 코로나 업데이트 시간 전
                                int newCnt, currDecideCnd;
                              
                                if (Covid19API.isCovid19Data(currentDate)) {
                                    int agoCnt = DBConnect.getCovid19Info(currentDate.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE));   // 어제 확진자 수
                                    currDecideCnd = DBConnect.getCovid19Info(currentDate.format(DateTimeFormatter.BASIC_ISO_DATE));   // 오늘 확진자 수
                                    newCnt = currDecideCnd - agoCnt;  // 신규 확진자 수
                                } else {
                                    JsonObject currentCovidInfo = Covid19API.getCovid19Data(currentDate);
                                    newCnt = Covid19API.getCovid19NewDecide(currentCovidInfo);  // 신규 확진자 수
                                    currDecideCnd = Covid19API.getCurrentCovid19Decide(currentCovidInfo);   // 오늘 확진자 수
                                }
                                    
                                HashMap<String, Object> covidInfo = new HashMap<>();
                                covidInfo.put("msg", "covid19");
                                covidInfo.put("newCnt", String.valueOf(newCnt));
                                covidInfo.put("currDecideCnd", String.valueOf(currDecideCnd));
                                out.println(Util.createJSON(200, covidInfo));
                                System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Covid-19 Data Send Success!"));
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println("Conversion Failed!" + e.getMessage());
                                out.println(Util.createSingleKeyValueJSON(500, "msg", "covid19"));
                            }
                        }
                        /* 친구 새로고침 처리 */
                        else if(requestCode == 304) {
                            refreshHandler(out, user.getUserID());
                        }
                        /* 친구 요청 수락 처리*/
                        else if(requestCode == 305) {
                            friendReceiveHandler(jsonObject);
                        }
                        /* 닉네임 변경 처리 */
                        else if(requestCode == 306){
                            String friendRequestNick = jsonObject.get("requestNick").getAsString();
                            String friendRequestID = jsonObject.get("requestID").getAsString();
                            if(!friendRequestNick.isEmpty()){
                                if(DBConnect.nickCheck(friendRequestNick)){
                                    if(DBConnect.resetNickname(friendRequestID, friendRequestNick)){
                                        out.println(Util.createSingleKeyValueJSON(200, "msg", "nickreset"));
                                        System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Reset Nickname Success!"));
                                    }
                                    else{
                                        out.println(Util.createSingleKeyValueJSON(500, "msg", "nickreset"));
                                    }
                                }
                                else{
                                    out.println(Util.createSingleKeyValueJSON(401, "msg", "nickreset"));
                                }
                            }
                            else{
                                out.println(Util.createSingleKeyValueJSON(400, "msg", "nickreset"));
                            }
                        }
                        /* 상태 메세지 설정 */
                        else if(requestCode == 307){
                            String friendRequestMSG = jsonObject.get("requestMSG").getAsString();
                            String friendRequestID = jsonObject.get("requestID").getAsString();
                            if(DBConnect.resetStatusmsg(friendRequestID, friendRequestMSG)){
                                out.println(Util.createSingleKeyValueJSON(200, "msg", "statusmsgreset"));
                                System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Reset StatusMSG Success!"));
                            }
                            else{
                                out.println(Util.createSingleKeyValueJSON(500, "msg", "statusmsgreset"));
                            }
                        }
                        /* 비밀번호 변경 */
                        else if(requestCode == 308){
                            String friendRequestPW = jsonObject.get("requestPW").getAsString();
                            String friendRequestID = jsonObject.get("requestID").getAsString();
                            if(DBConnect.resetPassword(friendRequestID, friendRequestPW)){
                                out.println(Util.createSingleKeyValueJSON(200, "msg", "pwreset"));
                                System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Reset PW Success!"));
                            }
                            else{
                                out.println(Util.createSingleKeyValueJSON(500, "msg", "pwreset"));
                            }
                        }
                        /* 생일 변경 */
                        else if(requestCode == 309){
                            String friendRequestBirth = jsonObject.get("requestBirth").getAsString();
                            String friendRequestID = jsonObject.get("requestID").getAsString();

                            if(DBConnect.resetBirthday(friendRequestID, friendRequestBirth)){
                                out.println(Util.createSingleKeyValueJSON(200, "msg", "birthdayreset"));
                                System.out.println(Util.createLogString("Friend", socket.getInetAddress().getHostAddress(), "Reset birthday Success!"));
                            }
                            else{
                                out.println(Util.createSingleKeyValueJSON(500, "msg", "birthdayreset"));
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
                }
            }
            catch (Exception e) {
                if(out != null) out.println(Util.createSingleKeyValueJSON(500, "msg", "Server Error"));
                e.printStackTrace();
                System.out.println("Friend Server Error! " + e.getMessage());
            }
        }

        /*
         * User Login Handler
         * @author Taehyun Park, Minjae Seon
         * @param jsonObject Client로부터 받은 JsonObject
         * @throws Exception 에러 예외처리
         */
        private boolean loginHandler(JsonObject jsonObject) throws Exception {
            String userToken = jsonObject.get("token").getAsString();

            user = DBConnect.AccessSessionWithToken(userToken);

            // 정보 로드 실패시
            if(user == null) {
                // 에러 발송 후 서버랑 연결 해제
                out.println(Util.createSingleKeyValueJSON(403, "msg", "Can't Find User Infomation!"));
                in.close();
                out.close();
                socket.close();
                return false;
            }

            JsonArray friendArray = new JsonArray();
            // friend 테이블의 행 가져옴
            ArrayList<User> friendUsers = DBConnect.AccessFriendTable(user.getUserID(),0);

            for (User friendUser : friendUsers) {
                if(userInfo.containsKey(friendUser.getUserID())) {
                    friendUser.setOnline(true);
                }
                friendArray.add(new Gson().toJson(friendUser, User.class));
            }

            // 서버로 유저 정보 전송
            HashMap<String, Object> userInfoMap = new HashMap<>();
            userInfoMap.put("id", user.getUserID());
            userInfoMap.put("nick", user.getUserNick());
            userInfoMap.put("userMsg", user.getUserMsg());
            userInfoMap.put("friend", friendArray.toString());
            out.println(Util.createJSON(200, userInfoMap));

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

        /*
         * 친구 새로 고침 Handler
         * @author Minjae Seon
         */
        private void refreshHandler(PrintWriter userWriter, String userID) {
            HashMap<String, Object> userInfoMap = new HashMap<>();
            try {
                JsonArray friendArray = new JsonArray();
                // friend 테이블의 행 가져옴
                ArrayList<User> friendUsers = DBConnect.AccessFriendTable(userID, 0);
                for (User friendUser : friendUsers) {
                    if(userInfo.containsKey(friendUser.getUserID())) {
                        friendUser.setOnline(true);
                    }
                    friendArray.add(new Gson().toJson(friendUser, User.class));
                }

                // 서버로 유저 정보 전송
                userInfoMap.put("msg", "friendrefresh");
                userInfoMap.put("friend", friendArray);
                userWriter.println(Util.createJSON(200, userInfoMap));
            }
            catch (Exception e) {
                // 에러 알림
                e.printStackTrace();
                userInfoMap.put("msg", "friendrefresh");
                out.println(Util.createJSON(500, userInfoMap));
            }
        }

        /*
         * 친구 요청 수락 / 거절 Handler
         * @author Minjae Seon
         */
        private void friendReceiveHandler(JsonObject jsonObject) {
            String msg = jsonObject.get("msg").getAsString();
            String userID = jsonObject.get("id").getAsString();
            String targetID = jsonObject.get("targetid").getAsString();

            if(msg.equals("ok")) {
                DBConnect.requestAccept(userID, targetID);
                refreshHandler(out, user.getUserID());
                if(userInfo.containsKey(targetID)) refreshHandler(userInfo.get(targetID), targetID);
            }
            else {
                DBConnect.removeFriendRelationship(userID, targetID);
            }
        }
    }
}
