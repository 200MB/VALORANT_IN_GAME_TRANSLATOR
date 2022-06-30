import org.apache.http.entity.StringEntity;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class Main {
    //todo: optimize code, both way translation
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        LockFileIO lockFileIO = new LockFileIO("C:/Users/bacho/AppData/Local/Riot Games/Riot Client/Config/lockfile");
        LocalApi api = new LocalApi(lockFileIO);

    }

}
