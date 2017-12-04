package xm.cloudweight.camera.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by black on 2017/8/11.
 */

public class FileUtil {

    public static String getFilePath(Context context, String dir) {
        String directoryPath;
        //判断SD卡是否可用
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            directoryPath = context.getExternalFilesDir(dir).getAbsolutePath();
        } else {
            //没内存卡就存机身内存
            directoryPath = context.getFilesDir() + File.separator + dir;
        }

        File file = new File(directoryPath);
        if (!file.exists()) {//判断文件目录是否存在
            file.mkdirs();
        }
        return directoryPath + File.separator;
    }

    //获取某个文件夹下的所有文件
    public static List<File> getFiles(File file) {
        List<File> fileList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) {
                File[] fs = f.listFiles();
                if (fs != null && f.listFiles().length > 0)
                    fileList.addAll(getFiles(f));
                else
                    fileList.add(f);
            }
        return fileList;
    }

    // 将字符串写入到文本文件中
    public static void write2File(String content, String filePath, String fileName) {
        // 每次写入时，都换行写
        String strContent = content + "\r\n";
        try {
            File file = makeFilePath(filePath, fileName);
            if (file == null)
                return;
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("black", "Error on write File:" + e);
        }
    }

    // 生成文件
    private static File makeFilePath(String filePath, String fileName) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
        try {
            file = new File(filePath + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
