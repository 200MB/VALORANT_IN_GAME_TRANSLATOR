import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ExternalIO {
    private JSONObject params = new JSONObject();

    public ExternalIO() throws FileNotFoundException {
        File file = new File("src/main/resources/parameters.txt");
        Scanner myReader = new Scanner(Objects.requireNonNull(file));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] split = data.split(":");
            params.put(split[0], split[1]);
        }
        myReader.close();
    }

    public JSONObject getParams() {
        return params;
    }
}
