import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Objects;
import java.util.Scanner;

public class ExternalIO {
    private final JSONObject params = new JSONObject();

    private File getFileFromDirectory() {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String jarDir = jarFile.getParentFile().getPath();
        File file = new File(jarDir + "\\parameters.txt");
        return file;
    }

    public ExternalIO() throws FileNotFoundException {
        Scanner myReader = new Scanner(Objects.requireNonNull(getFileFromDirectory()));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] split = data.split(":", 2);
            if (split[1].length() == 0) {
                System.out.println("PARAMETER FOR " + split[0] + " IS MISSING");
                continue;
            }
            params.put(split[0], split[1]);
        }
        myReader.close();
    }



    public JSONObject getParams() {
        return params;
    }
}
