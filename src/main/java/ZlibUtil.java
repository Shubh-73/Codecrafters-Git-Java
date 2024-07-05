import java.io.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class ZlibUtil {

    public static void compressFile(File raw, File compressed) throws IOException {

        try(InputStream inputStream = new FileInputStream(raw);
            /**
             * FileInputStream is a class which extends the InputStream class. The function of FileInputStream
             * is to read data from a file.
             *
             * InputStream class is generally used to read stream of bytes.
             */

            OutputStream outputStream = new DeflaterOutputStream(new FileOutputStream(compressed)))

        /**OutputStream outputStream = new DeflaterOutputStream(new FileOutputStream(compressed)))
         *
         * FileOutputStream is a class which extends the OutputStream class. The purpose of the FileOutPutStream
         * class is to write bytes streams to a file.
         *
         * In this call, FileOutputStream is wrapped with DeflaterOutputStream. The function of DeflaterOutputStream
         * is to perform compression of output stream. As a result, data is compressed first and then written to
         * the compressed file.
         *
         * */

        {
            shovelInOut(inputStream, outputStream);
        }
    }


    public static byte[] decompressFile(File compressed) throws IOException {

        /**
         *
         * the function takes a compressed file as input and returns byte array.
         *
         * InflaterInputStream class is used for the purpose of decompressing a stream.
         *
         * FileInputStream generates a stream of bytes from a file which is then decompressed.
         *
         * try method, takes InputStream and OutputStream. The OutputStream is of ByteArrayOutputStream type to collect
         * the decompressed data.
         *
         *
         * */

        try(InputStream inputStream = new InflaterInputStream(new FileInputStream(compressed));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            shovelInOut(inputStream, outputStream);
            return outputStream.toByteArray();

        }
    }

    private static void shovelInOut(InputStream inputStream, OutputStream outputStream) throws IOException {

        /**
         * the functions transfer the data from inputstream to outputstream, using a Buffer.
         *
         *Allocation of byte array buffer is made of 1024 bytes. This buffer would hold the data temporarily from input stream
         * before writing to the output stream.
         * An integer variable streamLength is declared which is equal to one reading of a buffer. The inputstream.read method
         * reads the bytes of data and returns the number of bytes actually read from the buffer. As soon as it reaches the end,
         * it returns -1, marking the end of the buffer.
         *
         * then the output stream writes the data from buffer till the streamLength which is offset by 0.
         *
         */
        byte[] buffer = new byte[1024];
        int streamLength;
        while ((streamLength = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, streamLength);

        }
    }
}
