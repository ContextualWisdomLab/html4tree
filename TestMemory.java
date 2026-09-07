import java.io.File;

public class TestMemory {
    public static void main(String[] args) throws Exception {
        File ignore_file = new File(".html4ignore");
        ignore_file.createNewFile();
        System.out.println(ignore_file.isFile());
    }
}
