import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

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

    public static boolean readTree( int argc, String[] argv) throws IOException {

        String flag = "";
        String treeSha;

        if (argc <= 2){
            System.err.println("Invalid Arguments");
            return false;
        }

        if (argc == 3){
            treeSha = argv[2];
        }
        else {
            treeSha = argv[3];
            flag = argv[2];
        }

        if (!flag.isEmpty() && !flag.equals("--name-only")){
            System.err.println("Invalid Arguments");
            return false;
        }

        String directoryName = treeSha.substring(0, 2);
        String fileName = treeSha.substring(2);

        String path = "./git/objects/" + directoryName + "/" + fileName;
        File file = new File(path);

        if (!file.exists()){
            System.err.println("File does not exist");
            return false;
        }

        String treeData = Commons.readFile(file);
        if (treeData == null){
            return false;
        }

        StringBuilder buff = new StringBuilder();
        if(!Commons.decompressObject(buff, treeData)){
            return false;
        }

        String trimmedData = buff.substring(buff.indexOf("\0") + 1);
        List<String> names = new ArrayList<>();

        String line;
        do{
            line = trimmedData.substring(0, trimmedData.indexOf("\0"));
            if (line.startsWith("40000")){
                names.add(line.substring(6));
            }
            else {
                names.add(line.substring(7));
            }
            trimmedData = trimmedData.substring(trimmedData.indexOf("\0") + 21);
        } while (trimmedData.length() > 1);

        Collections.sort(names);
        for (String name : names){
            System.out.println(name);
        }
        return true;

    }


}
