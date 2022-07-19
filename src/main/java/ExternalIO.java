import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ExternalIO {
    private final JSONObject params = new JSONObject();
    private final ArrayList<String> badWords = new ArrayList<>();

    private File getFileFromDirectory(String name) {
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        File jarFile = null;
        try {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String jarDir = jarFile.getParentFile().getPath();
        File file = new File(jarDir + "\\%s".formatted(name));
        return file;
    }

    public ExternalIO() throws FileNotFoundException {
        setParams();
        setBadWords();
    }

    public ArrayList<String> getBadWords() {
        return badWords;
    }

    private void setParams() throws FileNotFoundException {
        Scanner myReader = new Scanner(Objects.requireNonNull(getFileFromDirectory("parameters.txt")));
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

    private void setBadWords() throws FileNotFoundException {
        File file = getFileFromDirectory("badWords.txt");
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String word = scanner.nextLine();
            badWords.add(word);
        }
    }


    public JSONObject getParams() {
        return params;
    }
}
