import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;

public class BlobUtils {

    private BlobUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void createBlobObject(String filePath){
        try{
            File fileToStore = new File(filePath);
            String contentOfFile = Files.readString(fileToStore.toPath());

            int sizeOfContent = contentOfFile.getBytes().length;
            String blobContent = "blob" + sizeOfContent + "\0" + contentOfFile;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            byte[] hashOfBlob = digest.digest(blobContent.getBytes());
            String blobHash = bytesToHex(hashOfBlob);

            String blobFolderaName = blobHash.substring(0, 2);
            String blobFileName = blobHash.substring(2);

            byte[] output = new byte[100];
            Deflater compressor = new Deflater();
            compressor.setInput(blobContent.getBytes());
            compressor.finish();
            compressor.deflate(output);

            File objects = new File(".git/objects");
            File blobFolder = new File(objects, blobFolderaName);
            blobFolder.mkdirs();
            File blobFile = new File(blobFolder, blobFileName);
            blobFile.createNewFile();
            Files.write(blobFile.toPath(), output);
            System.out.println(blobHash);

        }
        catch (IOException | NoSuchAlgorithmException e){

            e.printStackTrace();

        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for(byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);

        }
        return hexString.toString();
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
