package xm.cloudweight.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.google.gson.Gson;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import xm.cloudweight.CheckInActivity;
import xm.cloudweight.MainActivity;
import xm.cloudweight.R;
import xm.cloudweight.SimilarActivity;
import xm.cloudweight.SortOutActivity;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.app.App;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.utils.LogUtils;
import xm.cloudweight.utils.bussiness.ScaleUtil;
import xm.cloudweight.utils.connect.NetBroadcastReceiver;
import xm.cloudweight.utils.connect.NetUtil;

/**
 * @author wyh
 * @Description:
 * @creat 2017/8/1
 */
public abstract class BaseActivity extends RxAppCompatActivity implements NetBroadcastReceiver.NetEvent, DialogInterface.OnDismissListener {
    public static NetBroadcastReceiver.NetEvent mNetWorkEvent;
    //网络类型
    private int netMobile;
    private Unbinder mBind;
    private AlertDialog mAlertDialog;
    private AnimationDrawable mAnimationDrawable;
    private boolean isCloseActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            //隐藏标题栏跟状态栏
            setTitleAndAppBar();
            //获取数据
            getDate();

            setContentView(R.layout.layout_base_activity);
            //设置标题信息
            setTitleInfo();

            mBind = ButterKnife.bind(this);
            initContentView();
            loadDate();
            mNetWorkEvent = this;
            inspectNet();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        finish();
    }

    /**
     * 设置标题栏是否显示
     */
    private void setTitleInfo() {
        LinearLayout llBase = (LinearLayout) findViewById(R.id.ll_base);
        llBase.addView(LayoutInflater.from(this).inflate(getLayoutId(), null));
        if (this instanceof MainActivity
                || this instanceof CheckInActivity
                || this instanceof SortOutActivity
                || this instanceof SimilarActivity) {
            LinearLayout llBaseTitle = (LinearLayout) findViewById(R.id.ll_base_title);
            llBaseTitle.setVisibility(View.VISIBLE);
            TextView tvBaseTitle = (TextView) findViewById(R.id.tv_base_title);
            tvBaseTitle.setText(getBaseTitle());
            TextClock tvClock = (TextClock) findViewById(R.id.tv_base_clock);
            tvClock.setFormat12Hour("yyyy-MM-dd hh:mm:ss, EEEE");
        }
    }

    /**
     * 隐藏标题栏和状态栏
     */
    private void setTitleAndAppBar() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getWindow();
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBind != null) {
            mBind.unbind();
        }
        mNetWorkEvent = null;
        mAnimationDrawable = null;
        mAlertDialog = null;
    }

    private void showP() {
        if (mAlertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialog);
            View v = LayoutInflater.from(this).inflate(R.layout.loading, null);
            mAnimationDrawable = (AnimationDrawable) (v.findViewById(R.id.img_loading)).getBackground();
            builder.setView(v);
            builder.setCancelable(true);
            mAlertDialog = builder.create();
            mAlertDialog.setOnDismissListener(this);
        }
        if (mAlertDialog != null && !mAlertDialog.isShowing()) {
            if (!mAnimationDrawable.isRunning()) {
                mAnimationDrawable.start();
            }
            mAlertDialog.show();
        }
    }

    private void dismissP() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAnimationDrawable.stop();
            mAlertDialog.dismiss();
        }
    }

    /**
     * 初始化时判断有没有网络
     */
    public boolean inspectNet() {
        this.netMobile = NetUtil.getNetWorkState(BaseActivity.this);
        return isNetConnect();
    }

    /**
     * 网络变化之后的类型
     */
    @Override
    public void onNetChange(int netMobile) {
        this.netMobile = netMobile;
        isNetConnect();
    }

    /**
     * 判断有无网络 。
     */
    public boolean isNetConnect() {
        if (netMobile == 1) {
            return true;
        } else if (netMobile == 0) {
            return true;
        } else if (netMobile == -1) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //体重秤清零
        if (this instanceof CheckInActivity
                || this instanceof SortOutActivity
                || this instanceof SimilarActivity) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_A:
                    break;
                case KeyEvent.KEYCODE_B:
                    Instrument instrument = ScaleUtil.getInstrument();
                    if (instrument != null) {
                        instrument.reset();
                    }
                    break;
                case KeyEvent.KEYCODE_C:
                    break;
                case KeyEvent.KEYCODE_D:
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void getDate() {

    }

    /**
     * 获取标题
     */
    public abstract String getBaseTitle();

    /**
     * 布局id
     */
    protected abstract int getLayoutId();

    /**
     * 初始化控件
     */
    protected abstract void initContentView();

    /**
     * 加载数据
     */
    protected abstract void loadDate();

    public Context getContext() {
        return this;
    }

    public BaseActivity getActivity() {
        return this;
    }

    public String getTag() {
        return getClass().getSimpleName();
    }

    public void showLog(String message) {
        showLog(getTag(), message);
    }

    public void showLog(Object obj) {
        showLog(new Gson().toJson(obj));
    }

    public void showLog(String tag, String message) {
        LogUtils.e(tag, message);
    }

    public void startActivity(Class c) {
        startActivity(new Intent(this, c));
    }

    public ApiManager getApiManager() {
        return App.getApiManager(getApplicationContext());
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        //首次加载点击尚未完成，点击取消返回首页
        if (isCloseActivity) {
            finish();
        }
    }

    protected void showLoadingDialog(boolean isClose) {
        isCloseActivity = isClose;
        showP();
    }

    protected void dismissLoadingDialog() {
        dismissP();
        isCloseActivity = false;
    }

}
