package xm.cloudweight;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.MD5Encoder;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.impl.LoginImpl;
import xm.cloudweight.presenter.CommPresenter;
import xm.cloudweight.presenter.LoginPresenter;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.RefreshMerchantHelper;

/**
 * @author wyh
 * @Description: 登录界面
 * @create 2017/10/25
 */
public class LoginActivity extends BaseActivity implements LoginImpl.OnLoginStateListener {

    @BindView(R.id.et_name)
    EditText mEtName;
    @BindView(R.id.et_pwd)
    EditText mEtPwd;
    @BindView(R.id.btn_login)
    Button mBtnLogin;

    @Override
    protected void getDate() {
        super.getDate();
        Merchant merchant = LocalSpUtil.getMerchant(this);
        if (merchant != null) {
            startActivity(MainActivity.class);
            finish();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initContentView() {

    }

    @Override
    protected void loadDate() {
        String user = LocalSpUtil.getUser(this);
        String pwd = LocalSpUtil.getPwd(this);
        if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
            mEtName.setText(user);
            mEtPwd.setText(pwd);
            mEtPwd.setSelection(pwd.length());
            mEtPwd.requestFocus();
        } else {
            mEtName.setText("");
            mEtPwd.setText("");
            mEtName.requestFocus();
        }

        //        15012345678  12345
        //        13806008655  12345
        //正式
        //        mEtName.setText("18559695718");
        //        mEtName.setText("13666049527");
        //        mEtPwd.setText("12345");
    }

    @OnClick(R.id.btn_login)
    public void onViewClicked() {
        String name = mEtName.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(pwd)) {
            //827CCB0EEA8A706C4C34A16891F84E7B
            pwd = MD5Encoder.MD5Encode(pwd).toUpperCase(Locale.getDefault());
            LoginPresenter.login(this, name, pwd);
        } else {
            ToastUtil.showShortToast(getContext(), "请输入完整信息");
        }
    }

    @Override
    public void onLoginStart() {
        showLoadingDialog(false);
    }

    @Override
    public void onLoginComplete() {
        dismissLoadingDialog();
    }

    @Override
    public void onLoginSuccess(Merchant merchant) {
        Context applicationContext = this.getApplicationContext();
        CommPresenter.getListWareHouse(applicationContext, merchant);
        CommPresenter.getDropDownLevels(applicationContext, merchant);

        String user = mEtName.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        LocalSpUtil.setUserInfo(getContext(), user, pwd);
        String jsonMerchant = GsonUtil.getGson().toJson(merchant);
        LocalSpUtil.setMerchant(getContext(), jsonMerchant);
        startActivity(MainActivity.class);
        finish();

        RefreshMerchantHelper.send(this, jsonMerchant);
    }

    @Override
    public void onLoginFaild(int errorType, String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public String getBaseTitle() {
        return null;
    }
}
