import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class Main {
    //todo: add a listener and translator
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        LockFileIO lockFileIO = new LockFileIO("C:/Users/bacho/AppData/Local/Riot Games/Riot Client/Config/lockfile");
        LocalApi api = new LocalApi(lockFileIO);


    }
}
