import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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

    public static String readTree(String treeSha, String... flags) throws IOException {


        byte[] rawContent = Commons.readGitObject(treeSha);
        //readGitObject is a static method in Commons class for providing a decompressed byte array of git files

        ByteBuffer byteBuffer = ByteBuffer.wrap(rawContent);
        byte[] objectName = new byte[4];
        byteBuffer.get(0,objectName,0,4);


        if(!new String(objectName).equals("tree")) {
            System.err.println("Not a tree");
            System.exit(1);
        }

        System.err.println("rawContent: " + new String(rawContent));
        System.err.println("rawContent.length: " + rawContent.length);

        int treeStartIndex = 5;

        int treeNullDelimiterIndex = findNullDelimiter(byteBuffer, treeStartIndex);

        byte[] treeBytes = new byte[treeNullDelimiterIndex - treeStartIndex];
        byteBuffer.get(treeStartIndex,treeBytes);

        int treeSize = Integer.parseInt(new String(treeBytes));
        System.err.println("treeSize: " + treeSize);

        List<TreeNode> treeEntries = (List<TreeNode>) readTreeNodeEntries(byteBuffer, treeNullDelimiterIndex);

        if(List.of(flags).contains("--name-only")){
            return treeEntries.stream()
                    .map(iterator -> iterator.name)
                    .collect(Collectors.joining("\n"));
        }


        return treeEntries.stream().map(
                iterator -> "%06d %s %s     %s".formatted(iterator.mode, gitObjectType(iterator.mode),
                        HexFormat.of().formatHex(iterator.sha), iterator.name)
        ).collect(Collectors.joining("\n"));



    }

    private static String gitObjectType(int mode){
        if(mode == 40000){
            return "tree";
        }
        else if(mode == 100644){
            return "blob";
        }

        return "not specified";
    }

    static class TreeNode{
        int mode;
        String name;
        byte[] sha;
        int entrySize;
        public TreeNode(int mode, String name, byte[] sha, int entrySize){

            this.mode = mode;
            this.name = name;
            this.sha = sha;
            this.entrySize = entrySize;

        }

        @Override
        public String toString(){
            return "TreeNode{"
                    + "mode=" + mode + ", name='" + name + '\'' +
                    ", sha=" + HexFormat.of().formatHex(sha) +
                    ", entrySize=" + entrySize + '}';
        }
    }

    private static int findNullDelimiter(ByteBuffer byteBuffer, int index) {
        int currentIndex = index;
        while(byteBuffer.hasRemaining() && byteBuffer.get(currentIndex) != '\0'){
            currentIndex++;
        }
        return currentIndex;
    }

    private static TreeNode readTreeNodeEntries(ByteBuffer byteBuffer, int startIndex){
        int entryNullDelimiter = findNullDelimiter(byteBuffer, startIndex);
        byte[] modeAndNameBytes = new byte[entryNullDelimiter - startIndex];
        byteBuffer.get(startIndex,modeAndNameBytes);
        String modeAndName = new String(modeAndNameBytes);


        int mode = Integer.parseInt(modeAndName.split(" ")[0]);
        String name = modeAndName.split(" ")[1];
        byte[] sha = new byte[20];
        byteBuffer.get(entryNullDelimiter + 1,sha);

        return new TreeNode(mode, name, sha,modeAndNameBytes.length + 1 + 20);
    }

    private static List<TreeNode> readTreeNodes(ByteBuffer byteBuffer, int treeNullDelimiterIndex){
        List<TreeNode> treeNodes = new ArrayList<>();

        int startIndex = treeNullDelimiterIndex + 1;
        while(startIndex < byteBuffer.limit()){
            TreeNode treeNode = readTreeNodeEntries(byteBuffer, startIndex);
            treeNodes.add(treeNode);
            startIndex = startIndex + treeNode.entrySize + 1;

        }

        return treeNodes;
    }
}
