import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

public class BlobUtils {

    private BlobUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static byte[] createBlobObject(String filePath){
        try {

            byte[] fileContent = Files.readAllBytes(Path.of(filePath));

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            buffer.write("blob".getBytes());

            buffer.write(" ".getBytes());

            buffer.write(String.valueOf(fileContent.length).getBytes());

            buffer.write(0);

            buffer.write(fileContent);

            byte[] blobContent = buffer.toByteArray();

            byte[] blobSHA = TreeGitUtils.toBinarySHA(blobContent);

            String blobPath = TreeGitUtils.shaToPath(TreeGitUtils.toHexSHA(blobSHA));

            File blobFile = new File(blobPath);

            blobFile.getParentFile().mkdirs();

            DeflaterOutputStream out = new DeflaterOutputStream(new FileOutputStream(blobFile));

            out.write(blobContent);

            out.close();
            return blobSHA;

        } catch (IOException e) {

            e.printStackTrace();

        }

        return null;

    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for(byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);

        }
        return hexString.toString();
    }

    public static String hexToBinary(String hex){
        StringBuilder binary  = new StringBuilder();

        for(char hexChar : hex.toCharArray()){
            String bin = Integer.toBinaryString(Character.digit(hexChar, 16));

            while (bin.length() < 4){
                bin = "0" + bin;
            }
            binary.append(bin);
        }
        return binary.toString();
    }

    /*

    Git Object : Content addressable filesystem. The name of file as stored by Git are
    mathematically derived from their contents. The implication of this is that a change of
    single byte in the content would change the name of the object.

    Files are not modified rather they are created and put in different location.

    */

    /*
        There are two major types of git objects -> Blob object abd Tree object.

        With respect to Blob object, there are two low level commands -> hash-object and cat-file

        1. git cat-file prints an existing git object to the standard output
        2. hash-object converts an existing file into a git object

    */
}
