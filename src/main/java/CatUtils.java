import java.io.*;
import java.util.zip.InflaterInputStream;

public class CatUtils {

    private CatUtils(){
        throw new IllegalStateException("Utility class");
    }

    public static void createCatFile(String filename){
        String directoryHash = filename.substring(0, 2);
        String fileHash = filename.substring(2, filename.length());

        File blobFile = new File("./.git/objects/" + directoryHash + "/" + fileHash);
        //creates a new file with a given path name which is then converted into abstract path

        try {
            String blob = new BufferedReader(new InputStreamReader
                    (new InflaterInputStream(new FileInputStream(blobFile)))).readLine();
            String content = blob.substring(blob.indexOf("\0") + 1);
            System.out.print(content);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }
}
