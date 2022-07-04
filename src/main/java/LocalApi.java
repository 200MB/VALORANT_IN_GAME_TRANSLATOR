import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
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
    ExternalIO externalIO;
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
    private String currentLoopState = "MENUS";
    private final boolean excludeHost; //init
    private final String translateTo; //init
    private final String nativeLanguage; //init
    private final JSONObject userInfo;
    private final String region;
    private final JSONObject params;

    public LocalApi(LockFileIO lockFileIO) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, ParseException, FileNotFoundException {
        this.lockFileIO = lockFileIO;
        this.externalIO = new ExternalIO();
        encodeBytes = Base64.getEncoder().encodeToString("riot:%s".formatted(lockFileIO.getPassword()).getBytes());
        setHttpClient();

        //init
        userInfo = getUserInfo();
        region = getRegion();
        params = externalIO.getParams();
        excludeHost = Boolean.parseBoolean((String) params.get("excludeHost"));
        translateTo = (String) params.get("translateTo");
        nativeLanguage = (String) params.get("nativeLanguage");
        System.out.println(excludeHost);
        System.out.println(translateTo);
        System.out.println(nativeLanguage);
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

    public String translate(String text, String to) throws ParseException {
        HttpPost request = new HttpPost("https://deep-translator-api.azurewebsites.net/google/");
        JSONParser parser = new JSONParser();
        request.addHeader("accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        JSONObject json = new JSONObject();
        json.put("source", "auto");
        json.put("target", to);
        json.put("text", text);
        StringEntity params = new StringEntity(json.toJSONString(), ContentType.APPLICATION_JSON);
        request.setEntity(params);
        System.out.println(getResponse(request));
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
        HttpGet request = new HttpGet(SESSION_GET.formatted(region, region, userInfo.get("sub")));
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

    public void sendChat(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"groupchat\"}".formatted(cid, message),ContentType.APPLICATION_JSON);
        request.setEntity(params);
        getResponse(request);

    }

    public String sendWhisper(String cid, String message) throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(SEND_WHISPER_URL.formatted(lockFileIO.getPort()));
        request.addHeader("Authorization", "Basic %s".formatted(encodeBytes));
        request.addHeader("Content-Type", "application/json");
        StringEntity params = new StringEntity("{\"cid\":\"%s\",\"message\":\"%s\",\"type\":\"chat\"}".formatted(cid, message),ContentType.APPLICATION_JSON);
        request.setEntity(params);
        return getResponse(request);

    }


    //checks before::
    public void sendTexts(ArrayList<String> texts) throws ParseException, UnsupportedEncodingException {
        for (String text : texts) {
            sendChat(determineCid(), text);
        }
    }

    private String getResponse(Object request) {
        StringBuilder jsonString = new StringBuilder();
        try (CloseableHttpResponse response = httpClient.execute((HttpUriRequest) request)) {

            // Get HttpResponse Status

            HttpEntity entity = response.getEntity();

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


    public JSONArray parseToJsonArray(String getJson, String external) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(getJson);

        return ((JSONArray) parser.parse(String.valueOf(object.get(external))));
    }


    public String getCid(String json) throws ParseException {
        return (String) parseToJson(json, "conversations").get("cid");
    }

    public ArrayList<String> getInGameChatChannels() throws ParseException {
        JSONParser parser = new JSONParser();
        JSONArray array = (JSONArray) ((JSONObject) parser.parse(getInGameChat())).get("conversations");
        JSONObject object1 = (JSONObject) parser.parse(String.valueOf(array.get(0)));
        JSONObject object2 = (JSONObject) parser.parse(String.valueOf(array.get(1)));
        ArrayList<String> cids = new ArrayList<>();
        cids.add((String) object2.get("cid"));
        cids.add((String) object1.get("cid"));
        return cids;
    }


    public String getInGameTeamChatCid() throws ParseException {
        return getInGameChatChannels().stream().filter(e -> e.contains("red@ares")).findFirst().orElseGet(()-> {
            try {
                return getInGameChatChannels().stream().filter(e->e.contains("blue@ares")).findFirst().get();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    public String getInGameAllChatCid() throws ParseException {
        return getInGameChatChannels().stream().filter(e -> e.contains("all@ares")).findFirst().get();

    }

    public ArrayList<String> getCombinedTextsInGame() throws ParseException {
        ArrayList<String> combined = new ArrayList<>();
        ArrayList<String> teamChat = getChatHistory(getInGameTeamChatCid());
        ArrayList<String> allChat = getChatHistory(getInGameAllChatCid());
        combined.addAll(teamChat);
        combined.addAll(allChat);
        return combined;
    }

    //checks before::
    public ArrayList<String> translateList(ArrayList<String> texts) throws ParseException {
        ArrayList<String> translatedTexts = new ArrayList<>();
        for (String text : texts) {
            String[] split = text.split(":");
            String translated = translate(split[1], translateTo);
            if (translated == null) continue;
            if (!translated.equalsIgnoreCase(split[1])) {
                translatedTexts.add(split[0] + ":" + translated);
            }
        }
        return translatedTexts;
    }
    //fdcfdfc5-c397-528c-9635-5bdcb4ade6de@tr1.pvp.net
    //gets messages from the current player session
    public ArrayList<String> determineRetrieval() throws ParseException, UnsupportedEncodingException {
        return switch (getLoopState()) {
            case "MENUS" -> getChatHistory(getCid(getPartyChatInfo()));
            case "PREGAME" -> getChatHistory(getCid(getPreGameChat()));
            case "INGAME" -> getCombinedTextsInGame();
            default -> null;
        };
    }


    public String determineCid() throws ParseException {
        return switch (getLoopState()) {
            case "MENUS" -> getCid(getPartyChatInfo());
            case "PREGAME" -> getCid(getPreGameChat());
            case "INGAME" -> getInGameTeamChatCid();
            default -> null;
        };
    }

    private boolean hasNewMessages(int size) {
        return this.size != size && size != 0;
    }

    //reads complete chat history from the current session and then continues where it was left off
    //updates both size and index.
    private ArrayList<String> filterFromIndex(ArrayList<String> list, int index) throws ParseException {
        ArrayList<String> arrayList = new ArrayList<>();
        for (int i = index; i < list.size(); i++) {
            arrayList.add(list.get(i));
        }
        this.size = list.size();
        this.index = list.size();
        System.out.println("resuming from " + index);
        System.out.println(arrayList);
        return translateList(arrayList);
    }

    public void updateLoopState() {
        try {
            if (!getLoopState().equalsIgnoreCase(currentLoopState)) {
                System.out.println("DETECTED SESSION CHANGE WAITING FOR CHAT TO LOAD...");
                Thread.sleep(5000); //waits for 5 seconds for chat to load. avoids possible nullpointer
                size = 0;
                index = 0;
                currentLoopState = getLoopState();
            }
        } catch (ParseException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getChatHistory(String cid) throws ParseException {
        JSONArray array = parseToJsonArray(getSpecificChat(cid), "messages");
        ArrayList<String> texts = new ArrayList<>();
        for (Object object : array) {
            String userPuuid = (String) ((JSONObject) object).get("puuid");
            String localUserPuuid = (String) getUserInfo().get("sub");

            String text =(((JSONObject) object).get("game_name")) + ":" + (((JSONObject) object).get("body"));
            if (!userPuuid.equalsIgnoreCase(localUserPuuid) && excludeHost) {
                texts.add(text);
            }
        }

        System.out.println(ANSI_YELLOW + "_" + RESET_ANSI);
        return texts;
    }


    //returns a chat history based on current session
    public ArrayList<String> retrieveText() {
        try {
            return determineRetrieval(); //already translated
        } catch (ParseException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onNewMessage(ArrayList<String> texts) throws UnsupportedEncodingException, ParseException {
        if (hasNewMessages(texts.size())) {
            texts = filterFromIndex(texts, index);
            try {
                sendTexts(texts);
            } catch (ParseException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void createListener() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateLoopState();
                ArrayList<String> texts = retrieveText();
                try {
                    onNewMessage(texts);
                } catch (UnsupportedEncodingException | ParseException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 2000);
    }

}
