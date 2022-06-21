
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Scanner;

public class LockFileIO { //name:pid:port:password:protocol
    private final String name;
    private final String pid;
    private final String port;
    private final String password;
    private final String protocol;
    private final String lockFileUrl;

    //find a lockfile for every user
    public LockFileIO(String lockFileUrl) throws IOException {
        this.lockFileUrl = lockFileUrl;
        File file = getLockFileCopy();
        StringBuilder allInOne = new StringBuilder();
        try {
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                allInOne.append(data);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("VALORANT IS NOT RUNNING");
        }

        String[] split = allInOne.toString().split(":");
        name = split[0];
        pid = split[1];
        port = split[2];
        password = split[3];
        protocol = split[4];
    }

    private File getLockFileCopy() throws IOException {
        File dest = new File("src/main/resources/lockfilecopy"); //generified
        try {
            File copied = new File(lockFileUrl);
            Files.copy(copied.toPath(), dest.toPath());
        } catch (FileAlreadyExistsException e) {
            dest.delete();
            getLockFileCopy();
        } catch (NoSuchFileException e) {
            System.out.println("VALORANT IS NOT RUNNING");
            return null;
        }
        return dest;
    }

    public String getName() {
        return name;
    }

    public String getPid() {
        return pid;
    }

    public String getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "LockFileIO{" +
                "name='" + name + '\'' +
                ", pid='" + pid + '\'' +
                ", port='" + port + '\'' +
                ", password='" + password + '\'' +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
