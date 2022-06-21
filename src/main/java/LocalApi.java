import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class LocalApi {
    LockFileIO lockFileIO;
    CloseableHttpClient httpClient;

    private static final String PARTY_CHAT_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-parties";
    private static final String SPECIFIC_CHAT_URL = "https://127.0.0.1:%s/chat/v6/messages?cid=%s"; //needs cid
    private static final String PRE_GAME_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-pregame";
    private static final String IN_GAME_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-coregame";
    private static final String SEND_URL = "https://127.0.0.1:%s/chat/v6/messages/";
    private static final String SEND_WHISPER_URL = "https://127.0.0.1:%s/chat/v6/messages/";
    private static final String GREEN_ANSI = "\\u001B[32m";
    private static final String RESET_ANSI = "\u001B[0m";
    private final String encodeBytes;

    public LocalApi(LockFileIO lockFileIO) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        this.lockFileIO = lockFileIO;
        encodeBytes = Base64.getEncoder().encodeToString("riot:%s".formatted(lockFileIO.getPassword()).getBytes());
        setHttpClient();

    }

    private void setHttpClient() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        this.httpClient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();
    }

    public void getPartyChatInfo() {
        HttpGet request = new HttpGet(PARTY_CHAT_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        getResponse(request);

    }

    public void getSpecificChat(String cid) {
        HttpGet request = new HttpGet(SPECIFIC_CHAT_URL.formatted(lockFileIO.getPort(), cid));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        getResponse(request);

    }

    public void getPreGameChat() {
        HttpGet request = new HttpGet(PRE_GAME_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        getResponse(request);

    }

    public void getInGameChat() {
        HttpGet request = new HttpGet(IN_GAME_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        getResponse(request);

    }

    public void sendChat(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"groupchat\"}".formatted(cid, message));
        request.setEntity(params);
        getResponse(request);

    }

    public void sendWhisper(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_WHISPER_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"chat\"}".formatted(cid, message));
        request.setEntity(params);
        getResponse(request);

    }

    private void getResponse(Object request) {
        try (CloseableHttpResponse response = httpClient.execute((HttpUriRequest) request)) {

            // Get HttpResponse Status
            System.out.println(response.getStatusLine().toString());

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();
            System.out.println(headers);

            // return it as a String
            String result = EntityUtils.toString(entity);
            System.out.println(result);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
