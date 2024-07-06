import javax.print.DocFlavor;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;
import java.nio.file.*;

import static java.lang.Long.compress;

public class TreeGitUtils {


    /**
     * from here we would return Tree object's content to the main class.
     * test input : ls-tree --name-only <tree_sha>
     *     args[0] -> command which will be used in switch case
     *     args[1] -> flag
     *     args[2] -> sha1 code
     */

    /**
     * we need to print the directory of a tree referenced by <tree_sha>
     *
     * output should be
     * <mode> <name> <sha>
     *  040000 is mode for Tree
     *  100644 is mode for Blob
     */

    /**
     * Structure of Tree Object Storage
     *
     * for a given sha code of a tree, first two characters indicate directory, rest, sha of object.
     *
     * example : tree_sha = e8sfbi90340293uankefbn393yak123.... , in this case, e8 is the directory.
     * it will be represented as :
     * .git/objects/e8/sfbi.....(remaining 38 chars)
     *
     * Once the tree object is decompressed, the structure will be :
     *
     *      tree <size>\0 : -> object header
     *      <mode> <name>\0<20_byte_sha> : -> entries of the objects in the tree
     *
     *
     */

    public static void readTree( String[] argv) throws IOException {

        String treeSha = argv[2];
        String treeDirectory = treeSha.substring(0,2);

        String treeHash = treeSha.substring(2);

        String filePath = ".git/objects/" + treeDirectory + "/" + treeHash;
        File file = new File(filePath);

        ArrayList<String> treeContent = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new
                InputStreamReader(new InflaterInputStream(new FileInputStream(file))))) {
            int data;
            while ((data = bufferedReader.read()) != -1) {
                if (data == 0) {
                    break;
                }
            }

            StringBuilder content = new StringBuilder();
            while ((data = bufferedReader.read()) != -1) {
                content.append((char) data);
            }

            String[] splitByModes = content.toString().split("100644|040000|100755|120000|40000");

            for (String splitByMode : splitByModes) {
                String[] res = splitByMode.split("\0");
                String trimmedData = res[0].trim();

                if (!trimmedData.isEmpty()) {
                    treeContent.add(trimmedData);
                }
            }

            Collections.sort(treeContent);

            for (String str : treeContent) {
                System.out.print(str + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static String writeTree(String directory) throws IOException {
        List<Map.Entry<String, String>> entries = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString();
                if (name.equals(".git")) continue;

                String fullPath = entry.toString();
                String mode = Files.isDirectory(entry) ? "40000" : "100644";
                String sha1Hash = Files.isDirectory(entry) ? writeTree(fullPath) : BlobUtils.createBlobObject(fullPath);

                if (!sha1Hash.isEmpty()) {
                    entries.add(new AbstractMap.SimpleEntry<>(name, mode + " " + name + '\0' + BlobUtils.hexToBinary(sha1Hash)));
                }
            }
        }

        entries.sort(Map.Entry.comparingByKey());

        StringBuilder treeContent = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            treeContent.append(entry.getValue());
        }

        String store = "tree " + treeContent.length() + '\0' + treeContent;
        String treeSha1 = sha1(store);
        String objPath = ".git/objects/" + treeSha1.substring(0, 2) + "/" + treeSha1.substring(2);

        Path objDir = Paths.get(objPath).getParent();
        if (objDir != null && !Files.exists(objDir)) {
            Files.createDirectories(objDir);
        }

        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get(objPath)))) {
            out.write(compress(store));
        }

        return treeSha1;
    }

    private static String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static byte[] compress(String str) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
            dos.write(str.getBytes());
            dos.finish();
            return bos.toByteArray();
        }
    }






}
