package com.black.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int databits, char event, int speed, int stopBit) throws SecurityException, IOException {
		/* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
//                Process su = Runtime.getRuntime().exec("/system/bin/su");
                Process su = Runtime.getRuntime().exec("su");
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead()
                        || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open(device.getAbsolutePath());
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }

        setConfig(databits, event, speed, stopBit);

        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path);

    /**
     * 设置串口数据位，校验位,速率，停止位
     *
     * @param databits 数据位 取值 位7或8
     * @param event    校验类型 取值N ,E, O,
     * @param speed    速率 取值 2400,4800,9600,115200
     * @param stopBit  停止位 取值1 或者 2
     * @return 1为成功；-1为失败
     */
    public native int setConfig(int databits, char event, int speed, int stopBit);

    public native void close();

    static {
        System.loadLibrary("SerialPort");
    }
}

