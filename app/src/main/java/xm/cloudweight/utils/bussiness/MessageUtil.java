package xm.cloudweight.utils.bussiness;

import android.os.Bundle;
import android.os.Message;

/**
 * @author wyh
 * @Description:
 * @creat 2018/1/22
 */
public class MessageUtil {

    public static Message create(long type, Object o) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putLong("Type", type);
        msg.setData(bundle);
        if (o != null) {
            msg.obj = o;
        }
        return msg;
    }

    public static String getObj(Message message) {
        return (String) message.obj;
    }

    public static long getType(Message message) {
        return message.getData().getLong("Type");
    }

}
