package xm.cloudweight.app;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.tencent.bugly.crashreport.CrashReport;

import xm.cloudweight.BuildConfig;
import xm.cloudweight.IRequestDataService;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.camera.service.CameraService;
import xm.cloudweight.net.RetrofitUtil;
import xm.cloudweight.service.BgOperateService;
import xm.cloudweight.service.RequestDataService;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.DBRequestManager;

/**
 * @author wyh
 * @Description:
 * @creat 2017/8/1
 */
public class App extends Application {

    private static ApiManager mApiManager;
    private static DBManager mDbManager;
    private static DBRequestManager mDbRequestManager;
    private Intent mCameraService;
    private Intent mImageUploadService;
    private Intent mRequestDataService;
    private static IRequestDataService mIRequestDataService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mIRequestDataService = IRequestDataService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();
//        QueryBuilder.LOG_VALUES = true;
//        QueryBuilder.LOG_SQL = true;

        CrashReport.initCrashReport(getApplicationContext(), "dfead879d1", BuildConfig.DEBUG);
        mCameraService = new Intent(this, CameraService.class);
        startService(mCameraService);

        mImageUploadService = new Intent(this, BgOperateService.class);
        startService(mImageUploadService);

        mRequestDataService = new Intent(this, RequestDataService.class);
        bindService(mRequestDataService, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(mCameraService);
        stopService(mImageUploadService);
        stopService(mRequestDataService);
        unbindService(mServiceConnection);
        mServiceConnection = null;
        mIRequestDataService = null;
    }

    public static ApiManager getApiManager(Context ctx) {
        if (mApiManager == null) {
            mApiManager = RetrofitUtil.getApiManager(ctx);
        }
        return mApiManager;
    }

    public static DBManager getDbManager(Context ctx) {
        if (mDbManager == null) {
            mDbManager = new DBManager(ctx.getApplicationContext());
        }
        return mDbManager;
    }

    public static DBRequestManager getDbRequestDataManager(Context ctx) {
        if (mDbManager == null) {
            mDbRequestManager = new DBRequestManager(ctx.getApplicationContext());
        }
        return mDbRequestManager;
    }

    public static IRequestDataService getIRequestDataService() {
        return mIRequestDataService;
    }
}
