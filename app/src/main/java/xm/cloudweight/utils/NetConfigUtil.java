package xm.cloudweight.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import xm.cloudweight.utils.bussiness.LocalSpUtil;

/**
 * @author wyh
 * @Description: 网络配置
 * @creat 2017/8/1
 */
public class NetConfigUtil {

    public static HashSet<String> mSetCookies = new HashSet<>();

    public static OkHttpClient getOkHttpClient(Context ctx) {
        File file = new File(ctx.getCacheDir(), "OkHttpCache");
        int cacheSize = 1024 * 1024 * 10;
        Cache cache = new Cache(file, cacheSize);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder
//                 .addNetworkInterceptor(new CacheInterceptor(ctx))
                //连接超时
                .connectTimeout(30, TimeUnit.SECONDS)
                //设置写超时
                .writeTimeout(20, TimeUnit.SECONDS)
                //设置读超时
                .readTimeout(20, TimeUnit.SECONDS)
                //是否自动重连
                .retryOnConnectionFailure(true)
                //添加头部
                .addInterceptor(new AddHeaderInterceptor(ctx))
                .addInterceptor(new GetHeaderInterceptor(ctx))
                // 缓存
                .cache(cache);
        if (true) {
            //log日志
            HttpLoggingInterceptor loggingInterceptor = getHttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }
        return builder.build();
    }

    private static HttpLoggingInterceptor getHttpLoggingInterceptor() {
        return new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                //打印retrofit日志
                if (!TextUtils.isEmpty(message)) {
                    if (message.startsWith("{\"errorCode") && message.endsWith("}")) {
                        LogUtils.e("===============", "======================================================================================================================================");
                        LogUtils.e("===============", message);
                        LogUtils.e("===============", "======================================================================================================================================");
                    } else {
                        LogUtils.e("--RetrofitLog--", " - -  " + message);
                    }
                }
            }
        });
    }

    /**
     * 获取头部
     */
    private static class GetHeaderInterceptor implements Interceptor {

        private Context ctx;

        private GetHeaderInterceptor(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            if (!originalResponse.headers("Set-cookie").isEmpty()) {
                final StringBuffer cookieBuffer = new StringBuffer();
                Observable.from(originalResponse.headers("Set-cookie"))
                        .map(new Func1<String, String>() {
                            @Override
                            public String call(String s) {
                                String[] cookieArray = s.split(";");
                                return cookieArray[0];
                            }
                        })
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String cookie) {
                                cookieBuffer.append(cookie);
                            }
                        });
                LocalSpUtil.setCookie(ctx, cookieBuffer.toString());
            }
            return originalResponse;
        }
    }

    /**
     * 添加头部
     */
    private static class AddHeaderInterceptor implements Interceptor {
        private Context ctx;

        private AddHeaderInterceptor(Context ctx) {
            this.ctx = ctx;
        }

        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            final Request.Builder builder = chain.request().newBuilder();
            Observable.just(LocalSpUtil.getCookie(ctx))
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String cookie) {
                            //添加cookie
                            if (!TextUtils.isEmpty(cookie)) {
                                builder.addHeader("Cookie", cookie);
                            }
                        }
                    });

            return chain.proceed(builder.build());
        }
    }

//    private static class CacheInterceptor implements Interceptor {
//
//        private final Context ctx;
//
//        public CacheInterceptor(Context ctx) {
//            this.ctx = ctx;
//        }
//
//        @Override
//        public Response intercept(Chain chain) throws IOException {
//            // 有网络时 设置缓存超时时间1个小时
//            int maxAge = 60 * 60;
//            // 无网络时，设置超时为1天
//            int maxStale = 60 * 60 * 24;
//            Request request = chain.request();
//            if (ConnectUtils.checkNetWorkIsAvailable(ctx)) {
//                //有网络时只从网络获取
//                request = request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
//            } else {
//                //无网络时只从缓存中读取
//                request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
//            }
//            Response response = chain.proceed(request);
//            if (ConnectUtils.checkNetWorkIsAvailable(ctx)) {
//                response = response.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public, max-age=" + maxAge)
//                        .build();
//            } else {
//                response = response.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
//                        .build();
//            }
//            return response;
//        }
//    }

}
