import java.io.*;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;

public class BlobUtils {
    public static byte[] createBlobObject(String filePath) {
        try {
            // Read file content
            byte[] fileContent = Files.readAllBytes(new File(filePath).toPath());

            // Prepare blob content with header
            String blobHeader = "blob " + fileContent.length + "\0";
            ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
            blobStream.write(blobHeader.getBytes());
            blobStream.write(fileContent);
            byte[] blobContent = blobStream.toByteArray();

            // Compute SHA-1 hash of the blob content
            byte[] sha = toBinarySHA(blobContent);

            // Convert binary SHA to hex string for path
            String shaHex = toHexSHA(sha);
            String blobPath = shaToPath(shaHex);

            // Ensure parent directories exist
            File blobFile = new File(blobPath);
            blobFile.getParentFile().mkdirs();

            // Write compressed blob content to file
            try (DeflaterOutputStream out = new DeflaterOutputStream(new FileOutputStream(blobFile))) {
                out.write(blobContent);
            }

            return sha;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String shaToPath(String sha) {
        return String.format(".git/objects/%s/%s", sha.substring(0, 2), sha.substring(2));
    }

    public static String toHexSHA(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static byte[] toBinarySHA(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
