package application.util;


import application.data.gather.companies.DataGatherer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by Admin on 30.06.2017.
 */
public class JsonUtil {

    public static JsonObject getJson(String url) {
        if (DataGatherer.internetProblem)
            return null;
        try {
            URL url_api = new URL(url);
            HttpURLConnection conn = ((HttpURLConnection) url_api.openConnection());
            InputStream inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JsonParser parser = new JsonParser();
            return ((JsonObject) parser.parse(reader));
        } catch (ConnectException e){
            //e.printStackTrace();
            System.out.println("Problems with connecting");
            DataGatherer.internetProblem = true;
        } catch (NoRouteToHostException e){
            //e.printStackTrace();
            System.out.println("Problems with connecting");
            DataGatherer.internetProblem = true;
        } catch (UnknownHostException e){
            //e.printStackTrace();
            System.out.println("Problems with connecting");
            DataGatherer.internetProblem = true;
        } catch (SocketException e){
            //e.printStackTrace();
            System.out.println("Problems with connecting");
            DataGatherer.internetProblem = true;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
