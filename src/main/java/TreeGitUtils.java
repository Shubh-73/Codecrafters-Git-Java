import javax.print.DocFlavor;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.InflaterInputStream;

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

        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new InflaterInputStream(
                    new FileInputStream(file)
            )));
            int data;
            while ((data = bufferedReader.read()) != -1) {
                if(data == 0){
                    break;
                }

            }
            StringBuilder content = new StringBuilder();
            boolean firstFlag = true;
            while((data = bufferedReader.read()) != -1){
                content.append((char) data);

            }

            String[] splitByModes = content.toString().split("100644|040000|100755|120000|40000");

            for(String splitByMode : splitByModes){
                String[] res = splitByMode.split("\0");
                String trimmedData = res[0];

                if(!trimmedData.isBlank()) treeContent.add(trimmedData);
            }

            bufferedReader.close();
            Collections.sort(treeContent);

            for (String str : treeContent) {
                System.out.println(str);
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }


    }


}
