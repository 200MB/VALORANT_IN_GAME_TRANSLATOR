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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

public class LocalApi {
    LockFileIO lockFileIO;
    CloseableHttpClient httpClient;
    private static final String PARTY_CHAT_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-parties"; //port
    private static final String SPECIFIC_CHAT_URL = "https://127.0.0.1:%s/chat/v6/messages?cid=%s"; //port,cid
    private static final String PRE_GAME_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-pregame"; //port
    private static final String IN_GAME_URL = "https://127.0.0.1:%s/chat/v6/conversations/ares-coregame"; //port
    private static final String SEND_URL = "https://127.0.0.1:%s/chat/v6/messages/"; //port
    private static final String SEND_WHISPER_URL = "https://127.0.0.1:%s/chat/v6/messages/"; //port
    private static final String RSO_RNet_GetEntitlementsToken = "https://127.0.0.1:%s/entitlements/v1/token"; //port
    private static final String RSO_RNet_GetUserInfoToken = "https://auth.riotgames.com/userinfo"; //port
    private static final String SESSION_GET = "https://glz-%s-1.%s.a.pvp.net/session/v1/sessions/%s"; //region,region,puuid
    private static final String REGION_GET = "https://127.0.0.1:%s/riotclient/region-locale"; //port
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String RESET_ANSI = "\u001B[0m";
    private final String encodeBytes;
    private Integer index = 0;
    private Integer size = 0;

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

    public String getPartyChatInfo() {
        return createGetRequest(PARTY_CHAT_URL);

    }

    public String translate(String text) throws UnsupportedEncodingException, ParseException {
        HttpPost request = new HttpPost("https://deep-translator-api.azurewebsites.net/google/");
        JSONParser parser = new JSONParser();
        request.addHeader("Content-Type", "application/json");
        request.addHeader("accept", "application/json");
        StringEntity params = new StringEntity("{\"source\":\"auto\",\"target\":\"en\",\"text\":\"%s\"}".formatted(text));
        request.setEntity(params);
        return (String) ((JSONObject) parser.parse(getResponse(request))).get("translation");
    }

    public String getSpecificChat(String cid) {
        HttpGet request = new HttpGet(SPECIFIC_CHAT_URL.formatted(lockFileIO.getPort(), cid));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        return getResponse(request);

    }

    public String getPreGameChat() {
        return createGetRequest(PRE_GAME_URL);

    }

    public String getInGameChat() {
        return createGetRequest(IN_GAME_URL);

    }

    public String getEntitlementsToken() {
        return createGetRequest(RSO_RNet_GetEntitlementsToken);
    }

    public JSONObject getUserInfo() throws ParseException {
        HttpGet request = new HttpGet(RSO_RNet_GetUserInfoToken);
        JSONParser parser = new JSONParser();
        String token = (String) ((JSONObject) parser.parse(getEntitlementsToken())).get("accessToken");
        request.addHeader("Authorization", "Bearer %s".formatted(token));
        return (JSONObject) parser.parse(getResponse(request));
    }

    public JSONObject getSession() throws ParseException {
        HttpGet request = new HttpGet(SESSION_GET.formatted(getRegion(), getRegion(), getUserInfo().get("sub")));
        JSONParser parser = new JSONParser();
        String accessToken = (String) ((JSONObject) parser.parse(getEntitlementsToken())).get("accessToken");
        String token = (String) ((JSONObject) parser.parse(getEntitlementsToken())).get("token");
        request.addHeader("Authorization", "Bearer %s".formatted(accessToken));
        request.addHeader("X-Riot-Entitlements-JWT", token);
        return (JSONObject) parser.parse(getResponse(request));
    }

    public String getLoopState() throws ParseException {
        return (String) getSession().get("loopState");
    }

    public String getRegion() throws ParseException {
        JSONParser parser = new JSONParser();
        String region = (String) ((JSONObject) parser.parse(createGetRequest(REGION_GET))).get("region");
        if (region.substring(0, 2).equalsIgnoreCase("eu")) return "eu";
        if (region.substring(0, 2).equalsIgnoreCase("na")) return "na";
        return "undefined";
    }


    private String createGetRequest(String URL) {
        HttpGet request = new HttpGet(URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        return getResponse(request);
    }

    public String sendChat(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"groupchat\"}".formatted(cid, message));
        request.setEntity(params);
        return getResponse(request);

    }

    public String sendWhisper(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_WHISPER_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"chat\"}".formatted(cid, message));
        request.setEntity(params);
        return getResponse(request);

    }

    public void sendTexts(ArrayList<String> texts) throws ParseException, UnsupportedEncodingException {
        for (String text : texts) {
            String translatedText = translate(text);
            sendChat(determineCid(), translatedText);
        }
    }

    private String getResponse(Object request) {
        StringBuilder jsonString = new StringBuilder();
        try (CloseableHttpResponse response = httpClient.execute((HttpUriRequest) request)) {

            // Get HttpResponse Status

            HttpEntity entity = response.getEntity();
            Header headers = entity.getContentType();

            // return it as a String
            String result = EntityUtils.toString(entity);
            jsonString.append(result);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString.toString();
    }

    public JSONObject parseToJson(String getJson, String external) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(getJson);
        object = (JSONObject) ((JSONArray) parser.parse(String.valueOf(object.get(external)))).get(0);
        return object;
    }

    public ArrayList<String> translateList(ArrayList<String> texts) throws UnsupportedEncodingException, ParseException {
        ArrayList<String> translatedTexts = new ArrayList<>();
        for (String text : texts) {
            translatedTexts.add(translate(text));
        }
        return translatedTexts;
    }

    public JSONArray parseToJsonArray(String getJson, String external) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(getJson);

        return ((JSONArray) parser.parse(String.valueOf(object.get(external))));
    }


    public String getCid(String json) throws ParseException {
        return (String) parseToJson(json, "conversations").get("cid");
    }

    public ArrayList<String> getChatHistory(String cid) throws ParseException {
        JSONArray array = parseToJsonArray(getSpecificChat(cid), "messages");
        ArrayList<String> texts = new ArrayList<>();
        for (Object object : array) {
            String userPuuid = (String) ((JSONObject) object).get("puuid");
            String localUserPuuid = (String) getUserInfo().get("sub");
            if (!userPuuid.equalsIgnoreCase(localUserPuuid)) {
                texts.add((String) ((JSONObject) object).get("body"));
            }
        }
        System.out.println(ANSI_YELLOW + "RETRIEVED CHAT" + RESET_ANSI);
        return texts;
    }

    public ArrayList<String> determineRetrieval() throws ParseException { //gets messages from the current player session
        return switch (getLoopState()) {
            case "MENUS" -> getChatHistory(getCid(getPartyChatInfo()));
            case "PREGAME" -> getChatHistory(getCid(getPreGameChat()));
            case "INGAME" -> getChatHistory(getCid(getInGameChat()));
            default -> null;
        };
    }

    public String determineCid() throws ParseException {
        return switch (getLoopState()) {
            case "MENUS" -> getCid(getPartyChatInfo());
            case "PREGAME" -> getCid(getPreGameChat());
            case "INGAME" -> getCid(getInGameChat());
            default -> null;
        };
    }

    private boolean hasNewMessages(int size) {
        return this.size != size;
    }

    //reads complete chat history from the current session and then continues where it was left off
    //updates both size and index.
    private ArrayList<String> filterFromIndex(ArrayList<String> list, int index) {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = index; i < list.size(); i++) {
            arrayList.add(list.get(i));
        }
        this.size = list.size();
        this.index = list.size();
        return arrayList;
    }


    public void createListener() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ArrayList<String> texts = new ArrayList<>();
                try {
                    texts = determineRetrieval();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (hasNewMessages(texts.size())) {
                    texts = filterFromIndex(texts, index);
                    try {
                        sendTexts(texts);
                    } catch (ParseException | UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 2000);
    }

}
