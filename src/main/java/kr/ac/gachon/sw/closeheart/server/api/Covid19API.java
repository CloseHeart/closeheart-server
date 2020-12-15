package kr.ac.gachon.sw.closeheart.server.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.ac.gachon.sw.closeheart.server.db.DBConnect;
import org.json.XML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Covid19API {
    private static String serviceKey = "";
    private static String serviceURL = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson";
    private static String currentDateStr = "";
    private static String agoDateStr = "";

    public static JsonObject getCovid19Data(LocalDate date) throws Exception {
        currentDateStr = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        agoDateStr = date.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        StringBuilder urlBuilder = new StringBuilder(serviceURL);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=2");
        urlBuilder.append("&" + URLEncoder.encode("startCreateDt", "UTF-8") + "=" + agoDateStr);
        urlBuilder.append("&" + URLEncoder.encode("endCreateDt", "UTF-8") + "=" + currentDateStr);

        URL apiURL = new URL(urlBuilder.toString());

        HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/xml");

        BufferedReader bufferedReader;
        String line;
        if(connection.getResponseCode()  >= 200 && connection.getResponseCode() <= 300) {
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder strBuilder = new StringBuilder();
            while((line = bufferedReader.readLine()) != null) {
                strBuilder.append(line);
            }

            String XMLtoJson = XML.toJSONObject(strBuilder.toString()).toString();
            JsonObject object = JsonParser.parseString(XMLtoJson).getAsJsonObject();

            bufferedReader.close();
            connection.disconnect();
            return object;
        }
        // 에러 발생시 서버 Console에 에러 출력
        else {
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            System.out.println("[Covid19API] Get API Error Occured!");

            while((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }

            bufferedReader.close();
            connection.disconnect();
            return null;
        }
    }
    /*
    * 해당 날짜의 신규 확진자 수 받기
    * @param 해당 날짜 Covid19API object
    * @return 신규 확진자 수
    * */
    public static int getCovid19NewDecide(JsonObject object) throws Exception{
        if(DBConnect.getCovid19Info(currentDateStr) == -1) {
            if(DBConnect.getCovid19Info(agoDateStr) == -1) {
                DBConnect.setCovid19Info(agoDateStr, getAgoCovid19Decide(object));
            }
            DBConnect.setCovid19Info(currentDateStr, getCurrentCovid19Decide(object));
        }
        int newDecide = DBConnect.getCovid19Info(currentDateStr) - DBConnect.getCovid19Info(agoDateStr);
        return newDecide;
    }
    /*
     * 해당 날짜의 확진자 수 받기
     * @param 해당 날짜 Covid19API object
     * @return 확진자 수
     * */
    public static int getCurrentCovid19Decide(JsonObject object) throws Exception{
        JsonObject currentObj = JsonParser.parseString(object.toString()).getAsJsonObject();
        JsonObject response = currentObj.get("response").getAsJsonObject();
        JsonObject body = response.get("body").getAsJsonObject();
        JsonObject items = body.get("items").getAsJsonObject();
        JsonArray item = items.get("item").getAsJsonArray();

        JsonObject curr = (JsonObject) item.get(0);
        int currDecideCnt = curr.get("decideCnt").getAsInt();  // 오늘 확진자 수
        return currDecideCnt;
    }
    /*
     * 하루 전 날의 확진자 수 받기
     * @param 해당 날짜 Covid19API object
     * @return 확진자 수
     * */
    public static int getAgoCovid19Decide(JsonObject object) throws Exception{
        JsonObject currentObj = JsonParser.parseString(object.toString()).getAsJsonObject();
        JsonObject response = currentObj.get("response").getAsJsonObject();
        JsonObject body = response.get("body").getAsJsonObject();
        JsonObject items = body.get("items").getAsJsonObject();
        JsonArray item = items.get("item").getAsJsonArray();

        JsonObject ago = (JsonObject) item.get(1);
        int agoDecideCnt = ago.get("decideCnt").getAsInt();  // 어제 확진자 수
        return agoDecideCnt;
    }
    public static boolean isCovid19Data(LocalDate date){
        String currStr = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        String agoStr = date.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);
        if(DBConnect.getCovid19Info(currStr) == -1 || DBConnect.getCovid19Info(agoStr) == -1){
            return false;
        }else{
            return true;
        }
    }
}
