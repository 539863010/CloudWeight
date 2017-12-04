package xm.cloudweight.presenter;

import com.xmzynt.storm.service.user.merchant.Merchant;

import xm.cloudweight.api.ApiSubscribe;
import xm.cloudweight.api.ResponseEntity;
import xm.cloudweight.api.TransformerHelper;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.PLogin;
import xm.cloudweight.impl.LoginImpl;
import xm.cloudweight.utils.bussiness.BeanUtil;

/**
 * @author wyh
 * @Description: 登录界面请求
 * @creat 2017/10/31
 */
public class LoginPresenter {

    /**
     * 请求登录接口
     */
    public static void login(final BaseActivity aty, String name, String pwd) {
        if (!(aty instanceof LoginImpl.OnLoginStateListener)) {
            return;
        }
        PLogin pLogin = BeanUtil.getPLogin(name, pwd);
        aty.getApiManager().login(pLogin).compose(new TransformerHelper<ResponseEntity<Merchant>>().get(aty))
                .subscribe(new ApiSubscribe<Merchant>() {

                    @Override
                    protected void onRequestStart() {
                        super.onRequestStart();
                        ((LoginImpl.OnLoginStateListener) aty).onLoginStart();
                    }

                    @Override
                    protected void onRequestCompleted() {
                        super.onRequestCompleted();
                        ((LoginImpl.OnLoginStateListener) aty).onLoginComplete();
                    }

                    @Override
                    protected void onResult(Merchant result) {
                        ((LoginImpl.OnLoginStateListener) aty).onLoginSuccess(result);
                    }

                    @Override
                    protected void onResultFail(int errorType,String errorString) {
                        ((LoginImpl.OnLoginStateListener) aty).onLoginFaild(errorType,errorString);
                    }
                });

    }

}
