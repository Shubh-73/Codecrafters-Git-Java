import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args) throws IOException {
    //System.out.println("Logs from your program will appear here!");


     final String command = args[0];
     switch (command) {
       case "init" -> {
         final File root = new File(".git");
         new File(root, "objects").mkdirs();
         new File(root, "refs").mkdirs();
         final File head = new File(root, "HEAD");

         try {
           head.createNewFile();
           Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
           System.out.println("Initialized git directory");
         } catch (IOException e) {
           throw new RuntimeException(e);
         }
       }
       case "cat-file" -> {
         String hashcode = args[2];
         CatUtils.createCatFile(hashcode);
       }
       case "hash-object" -> {
         BlobUtils.createBlobObject(args[2]);
       }
       case "ls-tree" -> {

         boolean nameOnly = false;
         String treeSha = "";
         for (String arg : args) {
           if (arg.equals("--name-only")) {
             nameOnly = true;
           } else if (!arg.equals("ls-tree")) {
             treeSha = arg;
           }
         }
         TreeGitUtils.readTree(treeSha, nameOnly);

       }
       case "write-tree" -> {
         File dir = new File(".");
         byte[] sha = TreeGitUtils.writeTree(dir);
         if (sha != null) {
           System.out.println("Tree SHA-1: " + BlobUtils.toHexSHA(sha));
         }

       }

       default -> System.out.println("Unknown command: " + command);
     }
  }
}
