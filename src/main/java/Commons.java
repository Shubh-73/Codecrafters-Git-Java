import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Commons {

    static byte[] readGitObject(String shaCode) throws IOException {
        String directoryName = shaCode.substring(0, 2);
        String fileName = shaCode.substring(2);

        /**
         * The function is taking SHA-1 hexadecimal value in form of a String.
         *
         * The SHA-1 (secure hash algorithm 1) is a cryptographic algorithm that produces 160 bit(20 byte) hash value,
         * which is commonly rendered as a 40-digit hexadecimal value.
         *
         * In git file system, first 2 digit correspond to the name of the directory of the file.
         * The rest of the hashcode represents the name of the file.
         */

        File objectDirectory = new File(".git/objects/" +directoryName);
        /**
         * File is created with the address of directory. This file will further store the blob objects as files.
         * File can be instantiated with single String parameter to specify the address of the file. Also, it can be
         * instantiated providing two string arguments specifying parent directory and the file's directory.
         */
        if (!objectDirectory.exists()) {
            System.out.println("Object directory does not exist");
            System.exit(1);
        }

        File file = new File(objectDirectory, fileName);

        Path content = Files.createTempFile("git-object-content", ".txt");

        /**
         * as the challenge required to provide a decompressed file, the ZlibUtil class is used to decompress the file
         * and returns byte array.
         */
        return ZlibUtil.decompressFile(file);


    }
}
