package isaacais;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Util {
    public static String getFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(path)));
        StringBuilder b = new StringBuilder();
        while(reader.ready()) {
            b.append(reader.readLine()).append("\n");
        }
        return b.toString();
    }
    public static InputStream readFile(String path) throws FileNotFoundException {
        return new FileInputStream(new File(path));
    }
    public static OutputStream writeFile(String path) throws URISyntaxException, FileNotFoundException {
        File file = new File(path);
        return new FileOutputStream(file);
    }
}
