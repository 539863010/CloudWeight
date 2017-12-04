package xm.cloudweight.api;

import rx.Observable;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.utils.TransformerUtil;

/**
 * @author wyh
 * @Description:  线程转换Helper
 * @creat 2017/10/24
 */
public class TransformerHelper<T> {

    public Observable.Transformer<T, T> get(final BaseActivity context) {
        try {
            return new Observable.Transformer<T, T>() {
                @Override
                public Observable<T> call(Observable<T> tObservable) {
                    return tObservable
                            .compose(TransformerUtil.<T>getIoMain())
                            .compose(TransformerUtil.<T>getStop(context));
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("context must instanceof RxActivity / RxAppcompatActivity / other Rx..components");
        }
    }


    public Observable.Transformer<T, T> get() {
        try {
            return new Observable.Transformer<T, T>() {
                @Override
                public Observable<T> call(Observable<T> tObservable) {
                    return tObservable
                            .compose(TransformerUtil.<T>getIo());
                }
            };
        } catch (Exception e) {
            throw new RuntimeException("context must instanceof RxActivity / RxAppcompatActivity / other Rx..components");
        }
    }

}
