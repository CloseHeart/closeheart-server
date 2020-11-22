package kr.ac.gachon.sw.closeheart.server.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.XML;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Covid19API {
    private static String serviceKey = "";
    private static String serviceURL = "http://openapi.data.go.kr/openapi/service/rest/Covid19/getCovid19InfStateJson";

    public static JsonObject getCovid19Data(String dateStr) throws Exception {

        StringBuilder urlBuilder = new StringBuilder(serviceURL);
        urlBuilder.append("?" + URLEncoder.encode("ServiceKey", "UTF-8") + "=" + serviceKey);
        urlBuilder.append("&" + URLEncoder.encode("pageNo", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("numOfRows", "UTF-8") + "=1");
        urlBuilder.append("&" + URLEncoder.encode("startCreateDt", "UTF-8") + "=" + dateStr);
        urlBuilder.append("&" + URLEncoder.encode("endCreateDt", "UTF-8") + "=" + dateStr);

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
}
