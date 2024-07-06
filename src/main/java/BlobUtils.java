import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;


public class BlobUtils {
    public static byte[] createBlobObject(String filePath) {
        try {
            File file = new File(filePath);
            byte[] content = new byte[(int) file.length()];
            try (FileInputStream fis = new FileInputStream(file)) {
                fis.read(content);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            buffer.write(("blob " + content.length + "\0").getBytes());
            buffer.write(content);
            byte[] blobContent = buffer.toByteArray();
            byte[] sha = toBinarySHA(blobContent);

            String blobPath = shaToPath(toHexSHA(sha));

            File blobFile = new File(blobPath);
            blobFile.getParentFile().mkdirs();

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
        byte[] sha = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            sha = md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha;
    }
}