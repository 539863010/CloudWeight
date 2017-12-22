package xm.cloudweight.fragment;

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
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.starnet.snview.component.liveview.LiveViewItemContainer;
import com.starnet.snview.protocol.Connection;
import com.starnet.snview.sdk.SNviewer;

import xm.cloudweight.ICameraService;
import xm.cloudweight.OnScreenshotListener;
import xm.cloudweight.R;
import xm.cloudweight.base.BaseFragment;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.camera.service.CameraService;
import xm.cloudweight.camera.util.NetUtil;
import xm.cloudweight.utils.bussiness.ScaleUtil;

/**
 * @author wyh
 * @description: 视频界面
 * @create 2017/12/3
 */
public class VideoFragment extends BaseFragment {

    private OnInstrumentListener mInstrumentListener;
    private ServiceConnection mServiceConnection;
    private ICameraService mICameraService;
    private SNviewer mSNviewer;
    private Instrument.OnReceive mInstrumentReceive = new Instrument.OnReceive() {
        @Override
        public void receive(Instrument.InsData data) {
            if (mInstrumentListener != null) {
                mInstrumentListener.receive(data);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_video;
    }

    @Override
    protected void getData() {

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void loadData() {
        //用于BaseActivity中f3清除
        ScaleUtil.startInstrument(mInstrumentReceive);
        startCamera();
    }

    private void startCamera() {
        mServiceConnection = new ServiceConnection() {

            @Override
            synchronized public void onServiceConnected(ComponentName name, IBinder service) {
                mICameraService = ICameraService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mICameraService = null;
            }
        };

        mSNviewer = new SNviewer(mActivity);
        mSNviewer.setDeviceParams("192.168.1.100", "8080", "admin", "123456", 0, "");
        ((FrameLayout) mView.findViewById(R.id.container_video)).addView(mSNviewer);
        mSNviewer.setConnectionStatusListener(new Connection.StatusListener() {
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
        });
        //开启摄像头
        NetUtil.setProcessDefaultNetwork(mActivity, ConnectivityManager.TYPE_ETHERNET);
        mActivity.bindService(new Intent(mActivity, CameraService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        if (mSNviewer != null) {
            mSNviewer.play();
        }

    }

    public boolean isLight() {
        if (mICameraService != null) {
            try {
                return mICameraService.isLight();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void stopCamera() {
        if (mSNviewer != null) {
            mSNviewer.stop();
        }
        if (mServiceConnection != null) {
            mActivity.unbindService(mServiceConnection);
        }
        NetUtil.setProcessDefaultNetwork(mActivity, null);
        mSNviewer = null;
        mServiceConnection = null;
        mICameraService = null;
    }

    public void screenshot(final Handler mHandlerShotPic) {
        if (mICameraService == null)
            return;
        if (mHandlerShotPic == null) {
            throw new RuntimeException("请设置Handler");
        }
        try {
            mICameraService.screenshot(new OnScreenshotListener.Stub() {
                @Override
                public void start() throws RemoteException {
                    mHandlerShotPic.sendEmptyMessage(1);
                }

                @Override
                public void success(String msg) throws RemoteException {
                    Message message = new Message();
                    message.what = 2;
                    Bundle bundle = new Bundle();
                    bundle.putString("path", msg);
                    message.setData(bundle);
                    mHandlerShotPic.sendMessage(message);
                }

                @Override
                public void error(String msg) throws RemoteException {
                    mHandlerShotPic.sendEmptyMessage(3);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ScaleUtil.stopInstrument();
        stopCamera();
    }

    public static VideoFragment getInstance() {
        return new VideoFragment();
    }

    public void setInstrumentListener(OnInstrumentListener instrumentListener) {
        mInstrumentListener = instrumentListener;
    }

    public interface OnInstrumentListener {

        void receive(Instrument.InsData data);

    }

}
