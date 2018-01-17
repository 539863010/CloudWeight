package xm.cloudweight.net;

import android.content.Context;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import xm.cloudweight.api.ApiManager;
import xm.cloudweight.utils.NetConfigUtil;

/**
 * @author wyh
 * @Description: 创建ApiManager
 * @creat 2018/1/17
 */
public class RetrofitUtil {

    public static ApiManager getApiManager(Context context) {
        return new Retrofit.Builder()
                .baseUrl(ApiManager.BASE_URL)
                .client(NetConfigUtil.getOkHttpClient(context))
//                    .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(ApiManager.class);
    }

}
