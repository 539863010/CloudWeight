package xm.cloudweight.utils.bussiness;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/6
 */
public class UploadPhotoUtil {

    private static final String end = "\r\n";
    private static final String boundary = "android";
    private static final String twoHyphens = "--";

    public static byte[] getFileByte(String filePath) {

        byte[] byte1 = begin(filePath);
        byte[] byte2 = file2byte(filePath);
        byte[] byte3 = end();

        byte[] finalByte = new byte[byte1.length + byte2.length + byte3.length];
        System.arraycopy(byte1, 0, finalByte, 0, byte1.length);
        System.arraycopy(byte2, 0, finalByte, byte1.length, byte2.length);
        System.arraycopy(byte3, 0, finalByte, byte1.length + byte2.length, byte3.length);
        return finalByte;
    }

    private static byte[] begin(String pngName) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(twoHyphens);
        buffer.append(boundary);
        buffer.append(end);
        buffer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + pngName + "\""
                + end);
        int length = pngName.length();
        buffer.append("image/" + pngName.substring(length - 3) + end);
        buffer.append(end);
        return buffer.toString().getBytes();
    }

    private static byte[] end() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(end);
        buffer.append(twoHyphens + boundary + twoHyphens + end);
        buffer.append(end);
        return buffer.toString().getBytes();
    }

    private static byte[] file2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
