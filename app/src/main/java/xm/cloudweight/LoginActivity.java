package xm.cloudweight;

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
import xm.cloudweight.presenter.LoginPresenter;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;

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
        } else {
            mEtName.setText("");
            mEtPwd.setText("");
        }

//        mEtName.setText("13666049527");
//        mEtName.setText("18559695718");
//        mEtPwd.setText("12345");
        mEtPwd.requestFocus();
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
        showP();
    }

    @Override
    public void onLoginComplete() {
        dismissP();
    }

    @Override
    public void onLoginSuccess(Merchant result) {
        String user = mEtName.getText().toString().trim();
        String pwd = mEtPwd.getText().toString().trim();
        LocalSpUtil.setUserInfo(getContext(), user, pwd);
        LocalSpUtil.setMerchant(getContext(), GsonUtil.getGson().toJson(result));
        startActivity(MainActivity.class);
        finish();
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