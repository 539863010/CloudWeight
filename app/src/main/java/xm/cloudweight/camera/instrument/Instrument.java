package xm.cloudweight.camera.instrument;


import android.os.Handler;
import android.os.Message;

import com.black.serialport.SerialPortUtil;

import java.text.DecimalFormat;

/**
 * Created by Black.L on 2016-10-24.
 */
public class Instrument implements SerialPortUtil.OnDataReceiveListener {

    private final static byte fh = (byte) 0xFA; //帧头
    private final static byte ef = (byte) 0xFB; //尾帧
    private final static byte[] BYTE_RESET = {fh, 0x02, 0x4b, 0x03, 0x48, ef}; //置零
    private final static byte[] BYTE_DEBARKING = {fh, 0x02, 0x4b, 0x02, 0x49, ef}; //去皮

    private InsData insData;
    private OnReceive onReceive;
    private Handler mHandler;

    private int nullCount;

    public interface OnReceive {
        void receive(InsData data);
    }

    public static class InsData implements Cloneable {
        public boolean debarked; //是否去皮
        public boolean zero; //是否归零
        public boolean stable; //是否稳定
        public String weight;
        public String price;
        public String summary;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    public Instrument() {
        insData = new InsData();
        initInsData();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                initInsData();
                if (msg.arg1 == 0) {
                    return;
                }
                resolve((byte[]) msg.obj);

                if (insData.weight.equals(""))
                    nullCount++;
                else {
                    if (onReceive != null)
                        onReceive.receive(insData);
                    nullCount = 0;
                }

                if (nullCount > 100) {
                    if (onReceive != null)
                        onReceive.receive(insData);
                    nullCount = 0;
                }
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void onDataReceive(byte[] buffer, int size) {
        Message msg = new Message();
        msg.arg1 = size;
        msg.obj = buffer;
        mHandler.sendMessage(msg);
    }

    public void setOnReceive(OnReceive onReceive) {
        this.onReceive = onReceive;
    }

    public void open() {
        SerialPortUtil.getInstance().openSerialPort("/dev/ttyMT2", 8, 'N', 9600, 1);
        SerialPortUtil.getInstance().setOnDataReceiveListener(this);
    }

    public void close() {
        SerialPortUtil.getInstance().setOnDataReceiveListener(null);
        onReceive = null;
        SerialPortUtil.getInstance().closeSerialPort();
    }

    public void setPrice(String price) {
        SerialPortUtil.getInstance().sendBuffer(price2Byte(price));
    }

    public void reset() {
        SerialPortUtil.getInstance().sendBuffer(BYTE_RESET);
    }

    public void debark() {
        SerialPortUtil.getInstance().sendBuffer(BYTE_DEBARKING);
    }

    private void resolve(byte[] bytes) {
        byte[] bits = validBytes(bytes);
        if (!isValid(bits))
            return;

        insData.stable = bits[2] == 0x30;
        switch ((char) bits[3]) {
            case 'N':
                insData.debarked = true;
                break;
            case 'G':
                break;
            case 'T':
                insData.debarked = true;
                insData.zero = true;
                break;
            case 'Z':
                insData.zero = true;
                break;
        }

        int signal = 0;
        switch (bits[4]) {
            case 0x2B:
                signal = 1;
                break;
            case 0x2D:
                signal = -1;
                break;
        }

        try {
            String str = "";
            for (int i = 5; i < 11; i++) {
                str += (char) bits[i];
            }
            if (!str.isEmpty()) {
                int decimal = Integer.valueOf("" + (char) bits[11]);
                insData.weight = (new DecimalFormat(decimal == 0 ? "0" : "0.00000".substring(0, decimal + 2))).format(signal * Double.valueOf(str) / Math.pow(10, decimal));
            } else
                insData.weight = "";

            str = "";
            for (int i = 13; i < 19; i++) {
                str += (char) bits[i];
            }
            if (!str.isEmpty())
                insData.price = (new DecimalFormat("0.00")).format(Double.valueOf(str) / 100);
            else
                insData.price = "";

            str = "";
            for (int i = 19; i < 27; i++) {
                str += (char) bits[i];
            }
            if (!str.isEmpty())
                insData.summary = (new DecimalFormat("0.00")).format(Double.valueOf(str) / 100);
            else
                insData.summary = "";
        } catch (NumberFormatException e) {
            insData.weight = "";
            insData.price = "";
            insData.summary = "";
        }
    }

    private void initInsData() {
        insData.debarked = false;
        insData.zero = false;
        insData.stable = false;
        insData.weight = "";
        insData.price = "";
        insData.summary = "";
    }

    private byte[] price2Byte(String price) {
        try {
            if (price.isEmpty() || Double.valueOf(price) < 0)
                return null;
        } catch (NumberFormatException e) {
            return null;
        }

        byte[] bytes = {fh, 0x08, 0x52, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, ef};
        int dot = price.indexOf(".");
        bytes[9] = dot == -1 ? 0x00 : (byte) (price.length() - dot);
        byte[] bs = price.replace(".", "").getBytes();
        for (int i = 0; i < bs.length; i++)
            bytes[8 - i] = bs[bs.length - i - 1];

        byte b = bytes[2];
        for (int i = 3; i < 10; i++) {
            b ^= bytes[i];
        }
        bytes[10] = b;

        return bytes;
    }

    private byte[] validBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;

        byte[] bits = new byte[29];

        int indexFh = -1;
        int i = 0;
        for (byte bit : bytes) {
            if (indexFh == -1 && bit == fh) {
                bits[i] = bit;
                indexFh = 0;
                i++;
            } else if (indexFh != -1) {
                bits[i] = bit;
                i++;
                if (i > bits.length - 1)
                    break;
            }
        }
        return bits;
    }

    private boolean isValid(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes[bytes.length - 1] != ef)
            return false;

        byte b = bytes[2];
        for (int i = 3; i < 27; i++) {
            b ^= bytes[i];
        }
        return b == bytes[27];
    }
}
