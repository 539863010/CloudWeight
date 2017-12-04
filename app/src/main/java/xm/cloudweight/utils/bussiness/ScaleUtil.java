package xm.cloudweight.utils.bussiness;

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
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.camera.service.CameraService;
import xm.cloudweight.camera.util.NetUtil;
import xm.cloudweight.utils.LogUtils;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/4
 */
public class ScaleUtil {

    private static ServiceConnection mServiceConnection;
    private static ICameraService mICameraService;
    private static SNviewer mSNviewer;
    private static Instrument mInstrument;

    public static void startCamera(final BaseActivity aty,int container) {
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

        mSNviewer = new SNviewer(aty);
        mSNviewer.setDeviceParams("192.168.1.100", "8080", "admin", "123456", 0, "");
        if(container != 0){
            ((FrameLayout) aty.findViewById(R.id.container)).addView(mSNviewer);
        }
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
        NetUtil.setProcessDefaultNetwork(aty, ConnectivityManager.TYPE_ETHERNET);
        aty.bindService(new Intent(aty, CameraService.class), mServiceConnection, Service.BIND_AUTO_CREATE);
        if (mSNviewer != null) {
            mSNviewer.play();
        }

    }

    public static boolean isLight(){
        if(mICameraService != null){
            try {
                return mICameraService.isLight();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void stopCamera(BaseActivity aty) {
        if (mSNviewer != null) {
            mSNviewer.stop();
        }
        if (mServiceConnection != null) {
            aty.unbindService(mServiceConnection);
        }
        NetUtil.setProcessDefaultNetwork(aty, null);
        mSNviewer = null;
        mServiceConnection = null;
        mICameraService = null;
    }

    public static void screenshot(final Handler mHandlerShotPic) {
        if (mICameraService == null)
            return;
        if(mHandlerShotPic == null){
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

    public static void startInstrument(Instrument.OnReceive receive) {
        try {
            mInstrument = new Instrument();
            mInstrument.open();
            mInstrument.setOnReceive(receive);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopInstrument() {
        if (mInstrument != null) {
            mInstrument.close();
        }
        mInstrument = null;
    }

    public static Instrument getInstrument() {
        return mInstrument;
    }
}
