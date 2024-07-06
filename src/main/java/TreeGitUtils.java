import javax.print.DocFlavor;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.*;
import java.nio.file.*;

import static java.lang.Long.compress;

public class TreeGitUtils {


    public static byte[] writeTree(File dir) {
        try {
            // Filter out .git directory
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return !name.equals(".git");
                }
            });

            // Sort files for consistent order
            Arrays.sort(files);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            // Process each file and directory
            for (File file : files) {
                if (file.isFile()) {
                    // Create blob object for file and get its SHA-1 hash
                    byte[] sha = BlobUtils.createBlobObject(file.getAbsolutePath());

                    // Write file mode, name, and SHA to buffer
                    buffer.write(("100644 " + file.getName() + "\0").getBytes());
                    buffer.write(sha);
                } else {
                    // Recursively process directory and get its SHA-1 hash
                    byte[] sha = writeTree(file);

                    // Write directory mode, name, and SHA to buffer
                    buffer.write(("40000 " + file.getName() + "\0").getBytes());
                    buffer.write(sha);
                }
            }

            byte[] treeItems = buffer.toByteArray();
            byte[] treeHeader = ("tree " + treeItems.length + "\0").getBytes();

            ByteBuffer combined = ByteBuffer.allocate(treeHeader.length + treeItems.length);
            combined.put(treeHeader);
            combined.put(treeItems);

            byte[] treeContent = combined.array();

            // Compute SHA-1 hash of the tree content
            byte[] treeSHA = BlobUtils.toBinarySHA(treeContent);

            // Convert binary SHA to hex string for path
            String treePath = BlobUtils.shaToPath(BlobUtils.toHexSHA(treeSHA));

            // Ensure parent directories exist
            File blobFile = new File(treePath);
            blobFile.getParentFile().mkdirs();

            // Write compressed tree content to file
            try (DeflaterOutputStream out = new DeflaterOutputStream(new FileOutputStream(blobFile))) {
                out.write(treeContent);
            }

            // Return binary SHA-1 hash
            return treeSHA;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void readTree(String[] argv) throws IOException {
        String treeSha = argv[1];
        String treeDirectory = treeSha.substring(0, 2);
        String treeHash = treeSha.substring(2);
        String filePath = ".git/objects/" + treeDirectory + "/" + treeHash;
        File file = new File(filePath);

        ArrayList<String> treeContent = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new InflaterInputStream(new FileInputStream(file))))) {
            int data;
            while ((data = bufferedReader.read()) != -1) {
                if (data == 0) {
                    break;
                }
            }
            StringBuilder content = new StringBuilder();
            boolean firstFlag = true;
            while ((data = bufferedReader.read()) != -1) {
                content.append((char) data);
            }

            String[] splitByModes = content.toString().split("100644|040000|100755|120000|40000");

            for (String splitByMode : splitByModes) {
                String[] res = splitByMode.split("\0");
                String trimmedData = res[0];

                if (!trimmedData.isBlank()) treeContent.add(trimmedData);
            }

            bufferedReader.close();
            Collections.sort(treeContent);

            for (String str : treeContent) {
                System.out.print(str + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
