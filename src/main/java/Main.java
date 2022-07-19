import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        ExternalIO externalIO = new ExternalIO();
        LockFileIO lockFileIO = new LockFileIO((String) externalIO.getParams().get("lockFileUrl"));
        LocalApi api = new LocalApi(lockFileIO);
        api.createListener();


    }
}
