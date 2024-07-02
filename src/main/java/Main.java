import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    // Uncomment this block to pass the first stage
    //
     final String command = args[0];
     //this reads the first input in the command line
     switch (command) {
       //if the first input is "init", then it means we need to create directory
       case "init" -> {
         //to create a directory, we need a root file
         final File root = new File(".git");
         //root file then now has multiple sub-files --> objects, refs, head
         new File(root, "objects").mkdirs();
         new File(root, "refs").mkdirs();
         final File head = new File(root, "HEAD");

         try {
           //now we want the system to check if the head file already exists in the system
           //if it does not exist, only then file will be created
           head.createNewFile();

           //head generally includes the metadata of the path therefore, we need to write that to
           //the head file. This can be done using the "write" method, which takes "Path" object as
           //input and bytes as input. Then it writes those bytes to that path.

           //"toPath" also throws an exception in case of invalid directory is passed as "Path"
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
       //if command is not "init"
       default -> System.out.println("Unknown command: " + command);
     }
  }
}
