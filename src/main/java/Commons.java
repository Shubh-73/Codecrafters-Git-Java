import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

public class Commons {



    public static String readFile(File file) {
        try(FileInputStream fileInputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean decompressObject(StringBuilder output, String data){
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data.getBytes());
            InflaterInputStream inflaterInputStream = new InflaterInputStream(byteArrayInputStream)){
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
                output.append(new String(buffer, 0, bytesRead));
            }
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }

    }
}
