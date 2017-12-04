package xm.cloudweight;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.sdk.SNviewer;

import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.camera.service.CameraService;
import xm.cloudweight.camera.util.NetUtil;

/**
 * @author wyh
 * @description: 测试 体重秤 接入
 * @create 2017/11/4
 */
@Deprecated
public class WeightActivity extends BaseActivity {

    private ServiceConnection mConnection;
    private SNviewer sNviewer;
    private Instrument instrument;
    private Handler mHandler;
    private ICameraService mICameraService;
    AppCompatRadioButton zeroRadio;
    AppCompatRadioButton debarkedRadio;
    AppCompatRadioButton stableRadio;
    TextView weightTv;
    private Intent mService;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_weight;
    }

    @Override
    protected void initContentView() {

    }

    @Override
    protected void loadDate() {

        zeroRadio = (AppCompatRadioButton) findViewById(R.id.zero);
        debarkedRadio = (AppCompatRadioButton) findViewById(R.id.debarked);
        stableRadio = (AppCompatRadioButton) findViewById(R.id.stable);
        weightTv = (TextView) findViewById(R.id.weight);

        mConnection = new ServiceConnection() {
            @Override
            synchronized public void onServiceConnected(ComponentName name, IBinder service) {
                mICameraService = ICameraService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mICameraService = null;
            }
        };

        sNviewer = new SNviewer(this);
        sNviewer.setDeviceParams("192.168.1.100", "8080", "admin", "123456", 0, "");
        sNviewer.setConnectionStatusListener(connectionStatusListener);
        ((FrameLayout) findViewById(R.id.video_frame)).addView(sNviewer);

        instrument = new Instrument();

        mHandler = new Handler() {

            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Toast.makeText(WeightActivity.this, "开始截屏", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(WeightActivity.this, "截屏成功, 图片路径: " + msg.getData().getString("path", ""), Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(WeightActivity.this, "截屏失败", Toast.LENGTH_LONG).show();
                        break;
                }
            }

        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mService);
    }

    @Override
    public String getBaseTitle() {
        return null;
    }

    public void open(View v) {
        NetUtil.setProcessDefaultNetwork(this, ConnectivityManager.TYPE_ETHERNET);
        mService = new Intent(this, CameraService.class);
        bindService(mService, mConnection, Service.BIND_AUTO_CREATE);
        sNviewer.play();
    }

    public void close(View v) {
        sNviewer.stop();
        unbindService(mConnection);
        NetUtil.setProcessDefaultNetwork(this, null);
    }

    public void screenshot(View v) {
        if (mICameraService == null)
            return;

        try {
            mICameraService.screenshot(new OnScreenshotListener.Stub() {
                @Override
                public void start() throws RemoteException {
                    mHandler.sendEmptyMessage(1);
                }

                @Override
                public void success(String msg) throws RemoteException {
                    Message message = new Message();
                    message.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("path", msg);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }

                @Override
                public void error(String msg) throws RemoteException {
                    mHandler.sendEmptyMessage(3);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void openYb(View v) {
        instrument.open();
        instrument.setOnReceive(receive);
    }

    public void closeYb(View v) {
        instrument.close();
        zeroRadio.setChecked(false);
        debarkedRadio.setChecked(false);
        stableRadio.setChecked(false);
        weightTv.setText("");
    }

    private final Connection.StatusListener connectionStatusListener = new Connection.StatusListener() {
        @Override
        public void OnConnectionTrying(View v) {
            //正在连接
            final LiveViewItemContainer c = (LiveViewItemContainer) v;
            c.setWindowInfoContent("正在连接...");
        }

        @Override
        public void OnConnectionFailed(View v) {
            Log.i("SNviewer", "OnConnectionFailed");
            final LiveViewItemContainer c = (LiveViewItemContainer) v;
            c.setWindowInfoContent("网络连接失败");
        }

        @Override
        public void OnConnectionEstablished(View v) {
            ((LiveViewItemContainer) v).setWindowInfoContent("连接建立");
        }

        @Override
        public void OnConnectionBusy(View v) {
            final LiveViewItemContainer c = (LiveViewItemContainer) v;
            if (c.isManualStop()) {
                return;
            }
        }

        @Override
        public void OnConnectionClosed(View v) {
            //连接关闭
            final LiveViewItemContainer c = (LiveViewItemContainer) v;
            c.setWindowInfoContent("视频连接已关闭");
            if (c.getConnection() == null || c.isManualStop()) {
                return;
            }
        }
    };

    private Instrument.OnReceive receive = new Instrument.OnReceive() {
        @Override
        public void receive(Instrument.InsData data) {
            if (data == null) {
                weightTv.setText("");
                return;
            }
            zeroRadio.setChecked(data.zero);
            debarkedRadio.setChecked(data.debarked);
            stableRadio.setChecked(data.stable);

            weightTv.setText(data.weight);
        }
    };
}
