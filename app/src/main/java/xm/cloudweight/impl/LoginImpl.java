package xm.cloudweight.impl;

import com.xmzynt.storm.service.user.merchant.Merchant;

/**
 * @author wyh
 * @Description: 登录返回接口
 * @creat 2017/10/31
 */
public class LoginImpl {

    public interface OnLoginStateListener{

        void onLoginStart();

        void onLoginComplete();

        void onLoginSuccess(Merchant result);

        void onLoginFaild(int errorType, String message);

    }

}
