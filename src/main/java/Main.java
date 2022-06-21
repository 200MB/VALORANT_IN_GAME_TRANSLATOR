

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class Main {
    //todo:parse result to json
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        LockFileIO lockFileIO = new LockFileIO("");
        LocalApi api = new LocalApi(lockFileIO);
        api.getPartyChatInfo();
    }
}
