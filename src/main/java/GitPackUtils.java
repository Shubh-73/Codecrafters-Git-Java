import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitPackUtils {

    public static void unpackPackFile(File packFile, String targetDir) {
        try {
            // Ensure target directory exists
            Path targetPath = Paths.get(targetDir);
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // Read from the pack file
            try (FileInputStream fis = new FileInputStream(packFile);
                 BufferedInputStream bis = new BufferedInputStream(fis)) {

                int totalObjects = readPackHeader(bis);

                System.out.println("Pack file content: ");
                printPackFileContent(bis, packFile.length());

                for (int i = 0; i < totalObjects; i++) {
                    long objectOffset = readObjectHeader(bis);
                    extractObject(bis, targetDir, objectOffset);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int readPackHeader(BufferedInputStream bis) throws IOException {
        byte[] header = new byte[12];
        if (bis.read(header) != 12) {
            throw new IOException("Invalid pack file header");
        }
        // Check 'PACK' signature and version
        if (header[0] != 'P' || header[1] != 'A' || header[2] != 'C' || header[3] != 'K' || header[4] != 0) {
            throw new IOException("Invalid pack file header");
        }
        int totalObjects = ((header[4] & 0xFF) << 24) | ((header[5] & 0xFF) << 16) |
                ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
        return totalObjects;
    }

    private static long readObjectHeader(BufferedInputStream bis) throws IOException {
        long objectOffset = 0;
        int type;
        long size;

        int b = bis.read();
        type = (b >> 4) & 7;
        size = b & 15;
        int shift = 4;
        int offset = 1;
        while ((b & 0x80) != 0) {
            b = bis.read();
            size += (b & 0x7f) << shift;
            shift += 7;
            offset++;
        }

        objectOffset = size;
        int headerByte = bis.read();

        while ((headerByte & 0x80) != 0) {
            objectOffset++;
            objectOffset <<= 8;
            objectOffset |= (headerByte & 0x7f);
            headerByte = bis.read();
        }
        return objectOffset;
    }

    private static void extractObject(BufferedInputStream bis, String targetDir, long objectOffset) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        File outFile = new File(targetDir, Long.toString(objectOffset));
        File parent = outFile.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(outFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
    }

    private static void printPackFileContent(BufferedInputStream bis, long length) throws IOException {
        byte[] content = new byte[(int) length];
        bis.read(content);
        System.out.println(new String(content));
        bis.reset(); // Reset stream position after reading for further processing
    }
}
