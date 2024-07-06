import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.DeflaterOutputStream;

public class CommitUtils {
    public static byte[] createCommitObject(String treeSha, String parentSha, String message, String authorName, String authorEmail) {
        try {
            String timestamp = getTimestamp();
            StringBuilder commitContent = new StringBuilder();
            commitContent.append("tree ").append(treeSha).append("\n");
            if (parentSha != null) {
                commitContent.append("parent ").append(parentSha).append("\n");
            }
            commitContent.append("author ").append(authorName).append(" <").append(authorEmail).append("> ").append(timestamp).append("\n");
            commitContent.append("committer ").append(authorName).append(" <").append(authorEmail).append("> ").append(timestamp).append("\n");
            commitContent.append("\n").append(message).append("\n");

            byte[] commitBytes = commitContent.toString().getBytes();
            String commitHeader = "commit " + commitBytes.length + "\0";
            ByteArrayOutputStream commitStream = new ByteArrayOutputStream();
            commitStream.write(commitHeader.getBytes());
            commitStream.write(commitBytes);
            byte[] commitObject = commitStream.toByteArray();

            byte[] sha = BlobUtils.toBinarySHA(commitObject);
            String shaHex = BlobUtils.toHexSHA(sha);
            String commitPath = BlobUtils.shaToPath(shaHex);

            File commitFile = new File(commitPath);
            commitFile.getParentFile().mkdirs();

            try (DeflaterOutputStream out = new DeflaterOutputStream(new FileOutputStream(commitFile))) {
                out.write(commitObject);
            }

            return sha;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }
}
