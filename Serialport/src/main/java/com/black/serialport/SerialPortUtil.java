package com.black.serialport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口操作类
 *
 * @author Jerome
 */
public class SerialPortUtil {
    private static SerialPortUtil mSerialPortUtil;

    private String TAG = SerialPortUtil.class.getSimpleName();
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    private OnDataReceiveListener onDataReceiveListener;
    private boolean running;

    private SerialPortUtil() {

    }

    public static SerialPortUtil getInstance() {
        if (mSerialPortUtil == null)
            synchronized (SerialPortUtil.class) {
                if (mSerialPortUtil == null)
                    mSerialPortUtil = new SerialPortUtil();
            }

        return mSerialPortUtil;
    }

    public interface OnDataReceiveListener {
        void onDataReceive(byte[] buffer, int size);
    }

    public void setOnDataReceiveListener(OnDataReceiveListener dataReceiveListener) {
        onDataReceiveListener = dataReceiveListener;
    }

    /**
     * 初始化串口信息
     */
    public boolean openSerialPort(String path, int databits, char event, int speed, int stopBit) {
        try {
            mSerialPort = new SerialPort(new File(path), databits, event, speed, stopBit);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();

            mReadThread = new ReadThread();
            mReadThread.start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 发送指令到串口
     *
     * @param cmd
     * @return
     */
    public boolean sendCmds(String cmd) {
        boolean result = true;
        byte[] mBuffer = cmd.getBytes();
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBuffer);
            } else {
                result = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean sendBuffer(byte[] mBuffer) {
        if (mBuffer == null || mBuffer.length == 0)
            return false;
        byte[] mBufferTemp = new byte[mBuffer.length];
        System.arraycopy(mBuffer, 0, mBufferTemp, 0, mBuffer.length);
        try {
            if (mOutputStream != null) {
                mOutputStream.write(mBufferTemp);
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendBuffer(String str) {
        return !str.isEmpty() && sendBuffer(hexStringToByte(str.replace(" ", "").toUpperCase()));
    }

    /**
     * 关闭串口
     */
    public void closeSerialPort() {
        onDataReceiveListener = null;
        if (mReadThread != null) {
            mReadThread.interrupt();
        }
        if (mSerialPort != null) {
            mSerialPort.close();
        }
    }

    private byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] aChar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(aChar[pos]) << 4 | toByte(aChar[pos + 1]));
        }
        return result;
    }

    private int toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            running = true;
            while (!isInterrupted()) {
                try {
                    if (mInputStream == null)
                        return;
                    byte[] buffer = new byte[512];
                    int size = mInputStream.read(buffer);
                    if (null != onDataReceiveListener) {
                        onDataReceiveListener.onDataReceive(buffer, size);
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            running = false;
        }
    }
}
