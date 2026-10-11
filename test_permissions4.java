import java.nio.file.Files;
import java.io.File;
import java.nio.file.attribute.PosixFilePermissions;

public class test_permissions4 {
    public static void main(String[] args) throws Exception {
        java.nio.file.Path tempPath = Files.createTempFile(new File(".").toPath(), ".index-", ".html", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")));
        System.out.println(Files.getPosixFilePermissions(tempPath));
        Files.deleteIfExists(tempPath);
    }
}
