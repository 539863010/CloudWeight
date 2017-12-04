package xm.cloudweight.utils;

import com.trello.rxlifecycle.ActivityEvent;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xm.cloudweight.base.BaseActivity;

/**
 * @author wyh
 * @Description: Transformer工具类
 * @creat 2017/10/24
 */
public class TransformerUtil {

    /**
     * 线程转换
     */
    public static <T> Observable.Transformer<T, T> getIoMain() {

        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {

                return tObservable
                        // 生产线程
                        .subscribeOn(Schedulers.io())
                        // 消费线程
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
    public static <T> Observable.Transformer<T, T> getIo() {

        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {

                return tObservable
                        // 生产线程
                        .subscribeOn(Schedulers.io());
            }
        };
    }

    /**
     * 防内存泄露
     */
    public static <T> Observable.Transformer<? super T, ? extends T> getStop(BaseActivity aty) {
        return aty.<T>bindUntilEvent(ActivityEvent.DESTROY);
    }

}
