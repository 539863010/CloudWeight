package xm.cloudweight.api;

import android.accounts.NetworkErrorException;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.xmzynt.storm.util.GsonUtil;

import org.json.JSONException;

import java.io.InterruptedIOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.concurrent.TimeoutException;

import rx.Subscriber;
import xm.cloudweight.comm.ServerConstant;
import xm.cloudweight.utils.TTypeUtil;

/**
 * @author wyh
 * @Description: 网络请求回调
 * @creat 2017/8/1
 */
public abstract class ApiSubscribe<T> extends Subscriber<ResponseEntity<T>> {


    @Override
    public void onNext(ResponseEntity<T> t) {
        if (t.getErrorCode() == 0) {
            String data = t.getData();
            if (!TextUtils.isEmpty(data)) {
                //返回数组
                Type type = TTypeUtil.getTListWithObject(this);
                if (type != null) {
                    if (!data.startsWith("{") && !data.startsWith("[")) {
                        //返回String
                        onResult((T) data);
                    } else {
                        //返回对象或数组
                        onResult((T) GsonUtil.getGson().fromJson(data, type));
                    }
                } else {
                    onResultFail(ServerConstant.ERROR_TYPE,"类型有误");
                }
            }else{
                onResult((T) data);
            }
        } else {
            onResultFail(ServerConstant.ERROR_MESSAGE,t.getMessage());
        }
    }

    @Override
    public void onError(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("请求失败：");
        if (e instanceof NetworkErrorException || e instanceof UnknownHostException || e instanceof ConnectException) {
            sb.append("网络连接异常,请检查网络!");
        } else if (e instanceof SocketTimeoutException || e instanceof InterruptedIOException || e instanceof TimeoutException) {
            sb.append("网络连接超时,请检查网络!");
        } else if (e instanceof JsonSyntaxException) {
            sb.append("网络请求异常,请检查网络!");
        } else if (e instanceof JsonParseException || e instanceof JSONException || e instanceof ParseException) {
            sb.append("数据请解析异常,请联系开发者!");
        } else {
            sb.append("未知异常,请联系开发者!");
        }
        onResultFail(ServerConstant.ERROR_SYSTEM,sb.toString());
    }

    @Override
    public void onStart() {
        super.onStart();
        onRequestStart();
    }

    @Override
    public void onCompleted() {
        onRequestCompleted();
    }

    protected void onRequestStart() {
    }

    protected void onRequestCompleted() {
    }

    /**
     * 请求回调
     * @param result 返回结果体
     */
    protected abstract void onResult(T result);

    /**
     * 请求错误
     * @param failString 错误信息
     */
    protected abstract void onResultFail(int errorType,String failString);

}

