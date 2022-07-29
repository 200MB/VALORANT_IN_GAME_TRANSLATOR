import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;


public class Main {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, ParseException {
        String lockFilePath = System.getenv("LOCALAPPDATA") + "\\Riot Games" + "\\Riot Client" + "\\Config" + "\\lockfile";
        if (!new File(lockFilePath).isFile()){
            throw new NoSuchFileException("VALORANT IS NOT RUNNING");
        }
        LockFileIO lockFileIO = new LockFileIO(lockFilePath);
        LocalApi api = new LocalApi(lockFileIO);
        api.createListener();


    }
}
