import java.nio.file.Files;
import java.io.File;
import java.nio.file.attribute.PosixFilePermissions;

public class test_permissions2 {
    public static void main(String[] args) throws Exception {
        java.nio.file.Path tempPath = Files.createTempFile(new File(".").toPath(), ".index-", ".html");
        System.out.println(Files.getPosixFilePermissions(tempPath));
        Files.deleteIfExists(tempPath);

        java.nio.file.attribute.FileAttribute<java.util.Set<java.nio.file.attribute.PosixFilePermission>> attr =
            PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));
        java.nio.file.Path tempPath2 = Files.createTempFile(new File(".").toPath(), ".index-", ".html", attr);
        System.out.println(Files.getPosixFilePermissions(tempPath2));
        Files.deleteIfExists(tempPath2);
    }
}
