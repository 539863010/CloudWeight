package xm.cloudweight.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.tencent.bugly.crashreport.CrashReport;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xm.cloudweight.BuildConfig;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.service.BgOperateService;
import xm.cloudweight.utils.NetConfigUtil;
import xm.cloudweight.camera.service.CameraService;

/**
 * @author wyh
 * @Description:
 * @creat 2017/8/1
 */
public class App extends Application {

    private static ApiManager mApiManager;
    private Intent mCameraService;
    private Intent mImageUploadService;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "dfead879d1", BuildConfig.DEBUG);
        mCameraService = new Intent(this, CameraService.class);
        startService(mCameraService);
        mImageUploadService = new Intent(this, BgOperateService.class);
        startService(mImageUploadService);
        initApiManager(getApplicationContext());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        stopService(mCameraService);
        stopService(mImageUploadService);
    }

    private static void initApiManager(Context ctx) {
        mApiManager = new Retrofit.Builder()
                .baseUrl(ApiManager.BASE_URL)
                .client(NetConfigUtil.getOkHttpClient(ctx))
//                    .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(ApiManager.class);
    }

    public static ApiManager getApiManager(Context ctx) {
        if (mApiManager == null) {
            initApiManager(ctx.getApplicationContext());
        }
        return mApiManager;
    }

}
