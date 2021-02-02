package requests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GETReply {
    // data
    private int statusCode;
    private String response;

    // constructor
    private GETReply(int statusCode, String response) {
        this.statusCode = statusCode;
        this.response = response;
    }

    // GET method
    public static GETReply sendGET(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return new GETReply(responseCode, response.toString());
        } else {
            return new GETReply(responseCode, "");
        }
    }

    // getters
    public int getStatusCode() {
        return statusCode;
    }
    public String getResponse() {
        return response;
    }
}