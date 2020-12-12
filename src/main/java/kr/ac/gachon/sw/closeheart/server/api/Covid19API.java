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

import static kr.ac.gachon.sw.closeheart.server.db.DBConnect.getCovid19Info;
import static kr.ac.gachon.sw.closeheart.server.db.DBConnect.setCovid19Info;

public class Covid19API {
    private static String serviceKey = "%2F62vvihbBAaUdKv4wHFwsSP6ZMNTNRpGE%2FZEurefpJYtCWzFM1blJ293Kb66k9GgndAigRBhKXLvdkjsbOKW1Q%3D%3D";
    private static String serviceURL = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson";
    private static String currentDateStr = "";
    private static String agoDateStr = "";

    public static JsonObject getCovid19Data(LocalDate date) throws Exception {
        currentDateStr = date.format(DateTimeFormatter.BASIC_ISO_DATE);
        agoDateStr = date.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

        StringBuilder urlBuilder = new StringBuilder(serviceURL);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
<<<<<<< HEAD
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=2");
        urlBuilder.append("&" + URLEncoder.encode("startCreateDt", "UTF-8") + "=" + dateStr);
        urlBuilder.append("&" + URLEncoder.encode("endCreateDt", "UTF-8") + "=" + dateStr);
=======
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("startCreateDt", "UTF-8") + "=" + agoDateStr);
        urlBuilder.append("&" + URLEncoder.encode("endCreateDt", "UTF-8") + "=" + currentDateStr);
>>>>>>> f9322ccabaabccf58a6270e1559c47c7a3598d4c

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

    public void getCovid19NewDecide(JsonObject object){
        if(DBConnect.getCovid19Info(currentDateStr) == -1){
            DBConnect.setCovid19Info(currentDateStr, getCurrentCovid19Decide(object));
            DBConnect.setCovid19Info(agoDateStr, getAgoCovid19Decide(object));
        }
        else {

        }
    }
    public int getCurrentCovid19Decide(JsonObject object){
        JsonObject currentObj = JsonParser.parseString(object.toString()).getAsJsonObject();
        JsonObject response = currentObj.get("response").getAsJsonObject();
        JsonObject body = response.get("body").getAsJsonObject();
        JsonObject items = body.get("items").getAsJsonObject();
        JsonArray item = items.get("item").getAsJsonArray();

        JsonObject curr = (JsonObject) item.get(0);
        int currDecideCnt = curr.get("DECIDE_CNT").getAsInt();  // 오늘 확진자 수
        return currDecideCnt;
    }
    public int getAgoCovid19Decide(JsonObject object){
        JsonObject currentObj = JsonParser.parseString(object.toString()).getAsJsonObject();
        JsonObject response = currentObj.get("response").getAsJsonObject();
        JsonObject body = response.get("body").getAsJsonObject();
        JsonObject items = body.get("items").getAsJsonObject();
        JsonArray item = items.get("item").getAsJsonArray();

        JsonObject ago = (JsonObject) item.get(1);
        int agoDecideCnt = ago.get("DECIDE_CNT").getAsInt();  // 어제 확진자 수
        return agoDecideCnt;
    }
}
