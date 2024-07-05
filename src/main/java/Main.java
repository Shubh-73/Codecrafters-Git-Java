import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args){
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
         try {
           if (args.length == 3) {
             String content = TreeGitUtils.readTree(args[2], args[1]);
             System.out.println(content);
           } else if (args.length == 2) {
             String content = TreeGitUtils.readTree(args[1]);
             System.out.println(content);

           }
         }
         catch (IOException e) {
           throw new RuntimeException(e);
         }

       }

       default -> System.out.println("Unknown command: " + command);
     }
  }
}
