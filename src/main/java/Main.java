import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
  public static void main(String[] args) throws IOException {
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
        String filePath = args[2];
        byte[] sha = BlobUtils.createBlobObject(filePath);
        if (sha != null) {
          System.out.println(BlobUtils.toHexSHA(sha));
        } else {
          System.err.println("Failed to create blob object.");
        }
      }
      case "ls-tree" -> {
        TreeGitUtils.readTree(args[2], args.length > 2 && "--name-only".equals(args[1]));
      }
      case "write-tree" -> {
        File dir = new File(".");
        byte[] sha = TreeGitUtils.writeTree(dir);
        if (sha != null) {
          System.out.println( BlobUtils.toHexSHA(sha));
        }
      }
      case "commit-tree" -> {
        String treeSha = args[1];
        String parentSha = null;
        String message = null;
        String authorName = "Your Name";
        String authorEmail = "you@example.com";

        for (int i = 2; i < args.length; i++) {
          if (args[i].equals("-p")) {
            parentSha = args[i + 1];
          } else if (args[i].equals("-m")) {
            message = args[i + 1];
          }
        }

        if (message == null) {
          System.err.println("Commit message is required");
          return;
        }

        byte[] sha = CommitUtils.createCommitObject(treeSha, parentSha, message, authorName, authorEmail);
        if (sha != null) {
          System.out.println(BlobUtils.toHexSHA(sha));
        } else {
          System.err.println("Failed to create commit object.");
        }
      }
      default -> System.out.println("Unknown command: " + command);
    }
  }
}
