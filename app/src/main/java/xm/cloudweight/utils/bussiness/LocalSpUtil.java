package xm.cloudweight.utils.bussiness;

import android.content.Context;
import android.text.TextUtils;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;

import xm.cloudweight.utils.SPUtils;

/**
 * @author wyh
 * @Description: 保存本地数据
 * @creat 2017/10/26
 */
public class LocalSpUtil {

    private static final String KEY_COOKIE = "cookie";
    private static final String KEY_MERCHANT = "merchant";

    public static Merchant getMerchant(Context context){
        String jsonMerchant = (String) SPUtils.get(context.getApplicationContext(), KEY_MERCHANT, "");
        if(TextUtils.isEmpty(jsonMerchant)){
            return null;
        }
        return GsonUtil.getGson().fromJson(jsonMerchant,Merchant.class);
    }

    public static void setMerchant(Context context,String jsonMerchant){
        SPUtils.put(context.getApplicationContext(), KEY_MERCHANT,jsonMerchant);
    }

    public static String getCookie(Context context){
        return (String) SPUtils.get(context.getApplicationContext(), KEY_COOKIE, "");
    }

    public static void setCookie(Context context, String cookie){
        SPUtils.put(context.getApplicationContext(), KEY_COOKIE,cookie);
    }


    private static final String KEY_USER = "user";
    private static final String KEY_PWD = "pwd";


    public static void setUserInfo(Context context,String user,String pwd){
        Context contextApplicationContext = context.getApplicationContext();
        SPUtils.put(contextApplicationContext, KEY_USER,user);
        SPUtils.put(contextApplicationContext, KEY_PWD,pwd);
    }

    public static String getUser(Context context){
        return (String) SPUtils.get(context.getApplicationContext(), KEY_USER, "");
    }

    public static String getPwd(Context context){
        return (String) SPUtils.get(context.getApplicationContext(), KEY_PWD,"");
    }

}
