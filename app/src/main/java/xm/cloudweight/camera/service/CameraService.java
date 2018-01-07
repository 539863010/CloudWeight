package xm.cloudweight.camera.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import xm.cloudweight.ICameraService;
import xm.cloudweight.OnScreenshotListener;
import xm.cloudweight.camera.constant.Constant;
import xm.cloudweight.camera.util.FileUtil;
import xm.cloudweight.camera.util.NetUtil;

/**
 * Created by Black.L on 2016-11-23.
 */
public class CameraService extends Service {

    public final String IP = "192.168.1.100";
    public final int PORT = 80;

    private boolean reset;
    private boolean light;
    private String station;
//    private Disposable timer;

    private CameraServiceImpl cameraServiceImpl = new CameraServiceImpl();

    private CameraCgi cameraCgi;
    //    private CompositeDisposable mCompositeDisposable;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public IBinder onBind(Intent intent) {
        return cameraServiceImpl;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        autoCheckConnection();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    /**
     * 校正时间
     */
    public void resetDate() {
        Calendar c = Calendar.getInstance();
        Map<String, String> params = new HashMap<>();
        params.put("action", "set");
        params.put("type", "1");
        params.put("manual.year", c.get(Calendar.YEAR) + "");
        params.put("manual.month", (c.get(Calendar.MONTH) + 1) + "");
        params.put("manual.day", c.get(Calendar.DATE) + "");
        params.put("manual.hour", c.get(Calendar.HOUR_OF_DAY) + "");
        params.put("manual.minute", c.get(Calendar.MINUTE) + "");
        params.put("manual.second", c.get(Calendar.SECOND) + "");
//        Disposable disposable =
//                cameraCgi.setDate(params)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe();
//        mCompositeDisposable.add(disposable);
        cameraCgi.setDate(params).subscribeOn(Schedulers.newThread()).subscribe();
    }

    /**
     * 更新通道名称
     *
     * @param chanName 通道名称
     */
    public void updateChanName(String chanName) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "set");
        try {
            params.put("channelname", URLEncoder.encode(chanName, "gb2312"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        cameraCgi.updateChanel(params)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
//        Disposable disposable = cameraCgi.updateChanel(params)
//                .subscribeOn(Schedulers.newThread())
//                .subscribeWith(new ResourceSubscriber<ResponseBody>() {
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        Log.d("fuck", "update channel name error");
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//        mCompositeDisposable.add(disposable);
    }

    /**
     * 截屏
     */
    public void getPic(final OnScreenshotListener callBack) {
        cameraCgi.screenshot().subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ResponseBody, String>() {
                    @Override
                    public String call(ResponseBody responseBody) {
                        String filePath = getFilePath();
                        Log.e("fuck", "pic file path:" + filePath);
                        if (writeResponseBodyToDisk(responseBody, filePath)) {
                            return filePath;
                        }
                        return "";
                    }
                }).doOnSubscribe(new Action0() {
            @Override
            public void call() {
                if (callBack != null)
                    try {
                        callBack.start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
            }
        }).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable t) {
                if (callBack != null)
                    try {
                        callBack.error(t.getMessage());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
            }

            @Override
            public void onNext(String str) {
                if (!str.isEmpty()) {
                    if (callBack != null)
                        try {
                            callBack.success(str);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                } else onError(new Throwable("抓拍失败"));
            }
        });

//        Disposable disposable = cameraCgi.screenshot()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Function<ResponseBody, String>() {
//                    @Override
//                    public String apply(ResponseBody responseBody) throws Exception {
//                        String filePath = getFilePath();
//                        Log.e("fuck", "pic file path:" + filePath);
//                        if (writeResponseBodyToDisk(responseBody, filePath)) {
//                            return filePath;
//                        }
//                        return "";
//                    }
//                })
//                .doOnSubscribe(new Consumer<Subscription>() {
//                    @Override
//                    public void accept(Subscription subscription) throws Exception {
//                        if (callBack != null)
//                            try {
//                                callBack.start();
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                    }
//                })
//                .subscribeWith(new ResourceSubscriber<String>() {
//                    @Override
//                    public void onNext(String str) {
//                        if (!str.isEmpty()) {
//                            if (callBack != null)
//                                try {
//                                    callBack.success(str);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                        } else onError(new Throwable("抓拍失败"));
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        if (callBack != null)
//                            try {
//                                callBack.error(t.getMessage());
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        //运行灯灭
        writeGpio(65, false);
        //关闭timer
//        if (timer != null && !timer.isDisposed())
//            timer.dispose();
//        mCompositeDisposable.dispose();
        unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }

    private void init() {
        initNet();
        cameraCgi = new Retrofit.Builder()
                .baseUrl("http://192.168.1.100/")
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                Log.d("fuck", String.format("发送请求 %s on %s%n", chain.request().url(), chain.connection()));
                                RequestBody requestBody = chain.request().body();
                                if (requestBody != null) {
                                    StringBuilder sb = new StringBuilder("Request Body [");
                                    Buffer buffer = new Buffer();
                                    requestBody.writeTo(buffer);
                                    Charset charset = Charset.forName("UTF-8");
                                    MediaType contentType = requestBody.contentType();
                                    if (contentType != null) {
                                        charset = contentType.charset(charset);
                                    }
                                    if (isPlaintext(buffer)) {
                                        sb.append(buffer.readString(charset));
                                        sb.append(" (Content-Type = ").append(contentType.toString()).append(",")
                                                .append(requestBody.contentLength()).append("-byte body)");
                                    } else {
                                        sb.append(" (Content-Type = ").append(contentType.toString())
                                                .append(",binary ").append(requestBody.contentLength()).append("-byte body omitted)");
                                    }
                                    sb.append("]");
                                    Log.d("fuck", String.format(Locale.getDefault(), "%s", sb.toString()));
                                }
                                Request request = chain.request()
                                        .newBuilder()
                                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                                        .header("Authorization", "Basic YWRtaW46MTIzNDU2")
                                        .build();
                                return chain.proceed(request);
                            }
                        })
                        .build())
                .build()
                .create(CameraCgi.class);

//        mCompositeDisposable = new CompositeDisposable();
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String station = intent.getStringExtra(Constant.BASIC_STATION_NAME);
                if (station != null) {
                    CameraService.this.station = station;
                    reset = false;
                }

                if (intent.getBooleanExtra(Constant.CAMERA_STOP, false))
                    stopSelf();
            }
        };
        //注册receiver，接收Activity发送的广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constant.ACTION_CAMERA);
        registerReceiver(mBroadcastReceiver, filter);

        SharedPreferences share = getSharedPreferences("setting", Context.MODE_WORLD_WRITEABLE);
        station = share.getString(Constant.BASIC_STATION_NAME, "");
    }

    static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private void initScreen() {
        resetDate();
        updateChanName(station);
    }

    private void initNet() {
        //设置默认网络为ETHERNET
        NetUtil.setProcessDefaultNetwork(this, ConnectivityManager.TYPE_ETHERNET);
    }

    public boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        try {
            // todo change the file location/name according to your needs
            File file = new File(fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[10240];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);
                int read;
                while ((read = inputStream.read(fileReader)) != -1) {
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d("fuck", "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 定时检测摄像机连接
     */
    private void autoCheckConnection() {
//        if (timer != null && !timer.isDisposed())
//            timer.dispose();
        Observable.interval(5, TimeUnit.SECONDS)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        boolean con;
                        try {
                            con = connect(IP, PORT);
                            if (light != con) {
                                light = con;
                                writeGpio(65, light);
                            }
                        } catch (SocketException e) {
                            initNet();
                            if (light) {
                                light = false;
                                writeGpio(65, false);
                            }
                            return;
                        }
                        //第一次连接初始化
                        if (!reset && con) {
                            reset = true;
                            initScreen();
                        }
                    }
                });
//        timer = Flowable.interval(0, 5, TimeUnit.SECONDS)
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        boolean con;
//                        try {
//                            con = connect(IP, PORT);
//                            if (light != con) {
//                                light = con;
//                                writeGpio(65, light);
//                            }
//                        } catch (SocketException e) {
//                            initNet();
//                            if (light) {
//                                light = false;
//                                writeGpio(65, false);
//                            }
//                            return;
//                        }
//                        //第一次连接初始化
//                        if (!reset && con) {
//                            reset = true;
//                            initScreen();
//                        }
//                    }
//                });
    }

    private String getFilePath() {
        String path = FileUtil.getFilePath(CameraService.this, "screen") + (new SimpleDateFormat("yyyy-MM-dd")).format(System.currentTimeMillis());
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }
//        return path + File.separator + (new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()) + ".jpg";
        //四位随机数
        int randomNum = (int) (Math.random() * 9000) + 1000;
        return path + File.separator + (new SimpleDateFormat("yyyyMMddHHmmssSSS")).format(randomNum) + ".jpg";
    }

    /**
     * 将GPIO写高或低
     *
     * @param gpio
     * @param high 是否高位
     */
    private void writeGpio(int gpio, boolean high) {
        try {
            writeFile("/sys/class/misc/mtgpio/pin", "-wmode " + gpio + " 0");
            writeFile("/sys/class/misc/mtgpio/pin", "-wdir " + gpio + " 1");
            writeFile("/sys/class/misc/mtgpio/pin", "-wdout " + gpio + " " + (high ? 1 : 0));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("fuck", "write gpio error:" + e.getMessage());
        }
    }

    /**
     * 写文件
     *
     * @param fileName
     * @param write_str
     * @throws IOException
     */
    private void writeFile(String fileName, String write_str) throws IOException {
        try {
            FileOutputStream fOut = new FileOutputStream(fileName);
            byte[] bytes = write_str.getBytes();

            fOut.write(bytes);
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * socket连接ip
     *
     * @param host ip地址
     * @param port 端口
     * @return
     */
    private synchronized boolean connect(String host, int port) throws SocketException {
        if (port == 0) port = 80;

        Socket connect = new Socket();
        try {
            Log.d("fuck", "connecting");
            connect.connect(new InetSocketAddress(host, port), 3 * 1000);
            return connect.isConnected();
        } catch (SocketException e) {
            Log.d("fuck", "SocketException");
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("fuck", "connect error:" + e.getMessage());
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private class CameraServiceImpl extends ICameraService.Stub {

        @Override
        public void setChannelName(String name) throws RemoteException {
            updateChanName(name);
        }

        @Override
        public void screenshot(final OnScreenshotListener callBack) throws RemoteException {
            getPic(new OnScreenshotListener() {
                @Override
                public IBinder asBinder() {
                    return null;
                }

                @Override
                public void start() throws RemoteException {
                    if (callBack != null)
                        callBack.start();
                }

                @Override
                public void success(String msg) throws RemoteException {
                    if (callBack != null)
                        callBack.success(msg);
                }

                @Override
                public void error(String msg) throws RemoteException {
                    if (callBack != null)
                        callBack.error(msg);
                }
            });
        }

        @Override
        public boolean isLight() throws RemoteException {
            return light;
        }

    }

//
//    public final String IP = "192.168.1.100";
//    public final int PORT = 80;
//
//    private boolean reset;
//    private boolean light;
//    private String station;
//    private Disposable timer;
//
//    private CameraServiceImpl cameraServiceImpl = new CameraServiceImpl();
//
//    private CameraCgi cameraCgi;
//    private CompositeDisposable mCompositeDisposable;
//    private BroadcastReceiver mBroadcastReceiver;
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return cameraServiceImpl;
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        init();
//
//        autoCheckConnection();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return START_REDELIVER_INTENT;
//    }
//
//    /**
//     * 校正时间
//     */
//    public void resetDate() {
//        Calendar c = Calendar.getInstance();
//        Map<String, String> params = new HashMap<>();
//        params.put("action", "set");
//        params.put("type", "1");
//        params.put("manual.year", c.get(Calendar.YEAR) + "");
//        params.put("manual.month", (c.get(Calendar.MONTH) + 1) + "");
//        params.put("manual.day", c.get(Calendar.DATE) + "");
//        params.put("manual.hour", c.get(Calendar.HOUR_OF_DAY) + "");
//        params.put("manual.minute", c.get(Calendar.MINUTE) + "");
//        params.put("manual.second", c.get(Calendar.SECOND) + "");
//        Disposable disposable = cameraCgi.setDate(params)
//                .subscribeOn(Schedulers.newThread())
//                .subscribe();
//        mCompositeDisposable.add(disposable);
//    }
//
//    /**
//     * 更新通道名称
//     *
//     * @param chanName 通道名称
//     */
//    public void updateChanName(String chanName) {
//        Map<String, String> params = new HashMap<>();
//        params.put("action", "set");
//        try {
//            params.put("channelname", URLEncoder.encode(chanName, "gb2312"));
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        Disposable disposable = cameraCgi.updateChanel(params)
//                .subscribeOn(Schedulers.newThread())
//                .subscribeWith(new ResourceSubscriber<ResponseBody>() {
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        Log.d("fuck", "update channel name error");
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//        mCompositeDisposable.add(disposable);
//    }
//
//    /**
//     * 截屏
//     *
//     * @param callBack
//     */
//    public void getPic(final OnScreenshotListener callBack) {
//        Disposable disposable = cameraCgi.screenshot()
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .map(new Function<ResponseBody, String>() {
//                    @Override
//                    public String apply(ResponseBody responseBody) throws Exception {
//                        String filePath = getFilePath();
//                        Log.e("fuck", "pic file path:" + filePath);
//                        if (writeResponseBodyToDisk(responseBody, filePath)) {
//                            return filePath;
//                        }
//                        return "";
//                    }
//                })
//                .doOnSubscribe(new Consumer<Subscription>() {
//                    @Override
//                    public void accept(Subscription subscription) throws Exception {
//                        if (callBack != null)
//                            try {
//                                callBack.start();
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                    }
//                })
//                .subscribeWith(new ResourceSubscriber<String>() {
//                    @Override
//                    public void onNext(String str) {
//                        if (!str.isEmpty()) {
//                            if (callBack != null)
//                                try {
//                                    callBack.success(str);
//                                } catch (RemoteException e) {
//                                    e.printStackTrace();
//                                }
//                        } else onError(new Throwable("抓拍失败"));
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        if (callBack != null)
//                            try {
//                                callBack.error(t.getMessage());
//                            } catch (RemoteException e) {
//                                e.printStackTrace();
//                            }
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
//        mCompositeDisposable.add(disposable);
//    }
//
//    @Override
//    public void onDestroy() {
//        //运行灯灭
//        writeGpio(65, false);
//        //关闭timer
//        if (timer != null && !timer.isDisposed())
//            timer.dispose();
//        mCompositeDisposable.dispose();
//        unregisterReceiver(mBroadcastReceiver);
//
//        super.onDestroy();
//    }
//
//    private void init() {
//        initNet();
//        cameraCgi = new Retrofit.Builder()
//                .baseUrl("http://192.168.1.100/")
//                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                .client(new OkHttpClient.Builder()
//                        .addInterceptor(new Interceptor() {
//                            @Override
//                            public Response intercept(Chain chain) throws IOException {
//                                Log.d("fuck", String.format("发送请求 %s on %s%n", chain.request().url(), chain.connection()));
//                                RequestBody requestBody = chain.request().body();
//                                if (requestBody != null) {
//                                    StringBuilder sb = new StringBuilder("Request Body [");
//                                    Buffer buffer = new Buffer();
//                                    requestBody.writeTo(buffer);
//                                    Charset charset = Charset.forName("UTF-8");
//                                    MediaType contentType = requestBody.contentType();
//                                    if (contentType != null) {
//                                        charset = contentType.charset(charset);
//                                    }
//                                    if (isPlaintext(buffer)) {
//                                        sb.append(buffer.readString(charset));
//                                        sb.append(" (Content-Type = ").append(contentType.toString()).append(",")
//                                                .append(requestBody.contentLength()).append("-byte body)");
//                                    } else {
//                                        sb.append(" (Content-Type = ").append(contentType.toString())
//                                                .append(",binary ").append(requestBody.contentLength()).append("-byte body omitted)");
//                                    }
//                                    sb.append("]");
//                                    Log.d("fuck", String.format(Locale.getDefault(), "%s", sb.toString()));
//                                }
//                                Request request = chain.request()
//                                        .newBuilder()
//                                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
//                                        .header("Authorization", "Basic YWRtaW46MTIzNDU2")
//                                        .build();
//                                return chain.proceed(request);
//                            }
//                        })
//                        .build())
//                .build()
//                .create(CameraCgi.class);
//
//        mCompositeDisposable = new CompositeDisposable();
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String station = intent.getStringExtra(Constant.BASIC_STATION_NAME);
//                if (station != null) {
//                    CameraService.this.station = station;
//                    reset = false;
//                }
//
//                if (intent.getBooleanExtra(Constant.CAMERA_STOP, false))
//                    stopSelf();
//            }
//        };
//        //注册receiver，接收Activity发送的广播
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Constant.ACTION_CAMERA);
//        registerReceiver(mBroadcastReceiver, filter);
//
//        SharedPreferences share = getSharedPreferences("setting", Context.MODE_WORLD_WRITEABLE);
//        station = share.getString(Constant.BASIC_STATION_NAME, "");
//    }
//
//    static boolean isPlaintext(Buffer buffer) {
//        try {
//            Buffer prefix = new Buffer();
//            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
//            buffer.copyTo(prefix, 0, byteCount);
//            for (int i = 0; i < 16; i++) {
//                if (prefix.exhausted()) {
//                    break;
//                }
//                int codePoint = prefix.readUtf8CodePoint();
//                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
//                    return false;
//                }
//            }
//            return true;
//        } catch (EOFException e) {
//            return false; // Truncated UTF-8 sequence.
//        }
//    }
//
//    private void initScreen() {
//        resetDate();
//        updateChanName(station);
//    }
//
//    private void initNet() {
//        //设置默认网络为ETHERNET
//        NetUtil.setProcessDefaultNetwork(this, ConnectivityManager.TYPE_ETHERNET);
//    }
//
//    public boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
//        try {
//            // todo change the file location/name according to your needs
//            File file = new File(fileName);
//            InputStream inputStream = null;
//            OutputStream outputStream = null;
//            try {
//                byte[] fileReader = new byte[10240];
//                long fileSize = body.contentLength();
//                long fileSizeDownloaded = 0;
//                inputStream = body.byteStream();
//                outputStream = new FileOutputStream(file);
//                int read;
//                while ((read = inputStream.read(fileReader)) != -1) {
//                    outputStream.write(fileReader, 0, read);
//                    fileSizeDownloaded += read;
//                    Log.d("fuck", "file download: " + fileSizeDownloaded + " of " + fileSize);
//                }
//                outputStream.flush();
//                return true;
//            } catch (IOException e) {
//                return false;
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            }
//        } catch (IOException e) {
//            return false;
//        }
//    }
//
//    /**
//     * 定时检测摄像机连接
//     */
//    private void autoCheckConnection() {
//        if (timer != null && !timer.isDisposed())
//            timer.dispose();
//        timer = Flowable.interval(0, 5, TimeUnit.SECONDS)
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        boolean con;
//                        try {
//                            con = connect(IP, PORT);
//                            if (light != con) {
//                                light = con;
//                                writeGpio(65, light);
//                            }
//                        } catch (SocketException e) {
//                            initNet();
//                            if (light) {
//                                light = false;
//                                writeGpio(65, false);
//                            }
//                            return;
//                        }
//                        //第一次连接初始化
//                        if (!reset && con) {
//                            reset = true;
//                            initScreen();
//                        }
//                    }
//                });
//    }
//
//    private String getFilePath() {
//        String path = FileUtil.getFilePath(CameraService.this, "screen") + (new SimpleDateFormat("yyyy-MM-dd")).format(System.currentTimeMillis());
//        File file = new File(path);
//        if (!file.exists()) {
//            file.mkdir();
//        }
//        return path + File.separator + (new SimpleDateFormat("yyyyMMddHHmmss")).format(System.currentTimeMillis()) + ".jpg";
//    }
//
//    /**
//     * 将GPIO写高或低
//     *
//     * @param gpio
//     * @param high 是否高位
//     */
//    private void writeGpio(int gpio, boolean high) {
//        try {
//            writeFile("/sys/class/misc/mtgpio/pin", "-wmode " + gpio + " 0");
//            writeFile("/sys/class/misc/mtgpio/pin", "-wdir " + gpio + " 1");
//            writeFile("/sys/class/misc/mtgpio/pin", "-wdout " + gpio + " " + (high ? 1 : 0));
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d("fuck", "write gpio error:" + e.getMessage());
//        }
//    }
//
//    /**
//     * 写文件
//     *
//     * @param fileName
//     * @param write_str
//     * @throws IOException
//     */
//    private void writeFile(String fileName, String write_str) throws IOException {
//        try {
//            FileOutputStream fOut = new FileOutputStream(fileName);
//            byte[] bytes = write_str.getBytes();
//
//            fOut.write(bytes);
//            fOut.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * socket连接ip
//     *
//     * @param host ip地址
//     * @param port 端口
//     * @return
//     */
//    private synchronized boolean connect(String host, int port) throws SocketException {
//        if (port == 0) port = 80;
//
//        Socket connect = new Socket();
//        try {
//            Log.d("fuck", "connecting");
//            connect.connect(new InetSocketAddress(host, port), 3 * 1000);
//            return connect.isConnected();
//        } catch (SocketException e) {
//            Log.d("fuck", "SocketException");
//            throw e;
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.d("fuck", "connect error:" + e.getMessage());
//        } finally {
//            try {
//                connect.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return false;
//    }
//
//    private class CameraServiceImpl extends ICameraService.Stub {
//
//        @Override
//        public void setChannelName(String name) throws RemoteException {
//            updateChanName(name);
//        }
//
//        @Override
//        public void screenshot(final OnScreenshotListener callBack) throws RemoteException {
//            getPic(new OnScreenshotListener() {
//                @Override
//                public IBinder asBinder() {
//                    return null;
//                }
//
//                @Override
//                public void start() throws RemoteException {
//                    if (callBack != null)
//                        callBack.start();
//                }
//
//                @Override
//                public void success(String msg) throws RemoteException {
//                    if (callBack != null)
//                        callBack.success(msg);
//                }
//
//                @Override
//                public void error(String msg) throws RemoteException {
//                    if (callBack != null)
//                        callBack.error(msg);
//                }
//            });
//        }
//
//    }
}
