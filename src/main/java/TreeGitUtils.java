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

    private static byte[] writeTree(File dir) {

        try {

            File[] files = dir.listFiles(new FilenameFilter() {

                @Override

                public boolean accept(File dir, String name) {

                    return !name.equals(".git");

                }

            });

            Arrays.sort(files);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            for (File file : files) {

                if (file.isFile()) {

                    byte[] sha = BlobUtils.createBlobObject(file.getAbsolutePath());

                    buffer.write(("100644 " + file.getName() + "\0")

                            .getBytes());

                    buffer.write(sha);

                } else {

                    byte[] sha = writeTree(file);

                    buffer.write(("40000 " + file.getName() + "\0")

                            .getBytes());

                    buffer.write(sha);

                }

            }

            byte[] treeItems = buffer.toByteArray();

            byte[] treeHeader =

                    ("tree " + treeItems.length + "\0").getBytes();

            ByteBuffer combined = ByteBuffer.allocate(

                    treeHeader.length + treeItems.length);

            combined.put(treeHeader);

            combined.put(treeItems);

            byte[] treeContent = combined.array();

            byte[] treeSHA = toBinarySHA(treeContent);

            String treePath = shaToPath(toHexSHA(treeSHA));

            File blobFile = new File(treePath);

            blobFile.getParentFile().mkdirs();

            DeflaterOutputStream out = new DeflaterOutputStream(

                    new FileOutputStream(blobFile));

            out.write(treeContent);

            out.close();
            return treeSHA;

        }
        catch (IOException e) {

            e.printStackTrace();

        }
        return null;
    }

    public static String shaToPath(String sha) {

        return String.format(".git/objects/%s/%s", sha.substring(0, 2),

                sha.substring(2));

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


    private static byte[] compress(String str) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             DeflaterOutputStream dos = new DeflaterOutputStream(bos)) {
            dos.write(str.getBytes());
            dos.finish();
            return bos.toByteArray();
        }
    }






}
