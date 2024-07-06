import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitCloneUtils {

    public static void cloneRepository(String repoUrl, String targetDir) {
        try {
            // Create target directory if it doesn't exist
            Path targetPath = Paths.get(targetDir);
            Files.createDirectories(targetPath);

            // Construct URL for Git clone
            URL url = new URL(repoUrl);
            String host = url.getHost();
            String path = url.getPath().endsWith("/") ? url.getPath() : url.getPath() + "/";
            String protocol = url.getProtocol();

            // Construct URL for Git clone request
            URL cloneUrl = new URL(protocol, host, 443, path + "info/refs?service=git-upload-pack");

            // Set up HTTP connection
            HttpURLConnection connection = (HttpURLConnection) cloneUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "GitCloneClient");

            // Check response status
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response to get Git protocol info
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder gitProtocolInfoBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    gitProtocolInfoBuilder.append(line).append("\n");
                }
                reader.close();

                String gitProtocolInfo = gitProtocolInfoBuilder.toString();
                if (!gitProtocolInfo.isEmpty()) {
                    // Clone using Git protocol info
                    cloneWithGitProtocol(repoUrl, targetDir, gitProtocolInfo);
                } else {
                    System.err.println("Failed to retrieve Git protocol info");
                }
            } else {
                System.err.println("Failed to connect to repository: " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void cloneWithGitProtocol(String repoUrl, String targetDir, String gitProtocolInfo) {
        try {
            // Construct URL for Git clone using Git protocol
            URL url = new URL(repoUrl);
            String host = url.getHost();
            String path = url.getPath().endsWith("/") ? url.getPath() : url.getPath() + "/";
            String protocol = url.getProtocol();

            URL cloneUrl = new URL(protocol, host, 443, path + "git-upload-pack");

            // Set up HTTP connection for Git protocol
            HttpURLConnection connection = (HttpURLConnection) cloneUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "GitCloneClient");
            connection.setRequestProperty("Content-Type", "application/x-git-upload-pack-request");

            // Enable output for POST requests
            connection.setDoOutput(true);

            // Construct request body for Git clone
            OutputStream out = connection.getOutputStream();
            String request = "0032want " + gitProtocolInfo.substring(4).trim() + "\n00000009done\n";
            out.write(request.getBytes());
            out.flush();
            out.close();

            // Check response status
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Save Git pack file to local repository
                File packFile = new File(targetDir, "repo.pack");
                try (InputStream in = connection.getInputStream();
                     FileOutputStream fos = new FileOutputStream(packFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                // Unpack Git pack file to create local repository
                GitPackUtils.unpackPackFile(packFile, targetDir);

                // Clean up
                packFile.delete();
            } else {
                System.err.println("Failed to clone repository: " + responseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        if (s.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
