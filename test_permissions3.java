import java.nio.file.Files;
import java.io.File;
import java.nio.file.attribute.PosixFilePermissions;

public class test_permissions3 {
    public static void main(String[] args) throws Exception {
        boolean supportsPosix = java.nio.file.FileSystems.getDefault().supportedFileAttributeViews().contains("posix");
        System.out.println(supportsPosix);
    }
}
