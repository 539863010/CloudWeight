package xm.cloudweight.utils.bussiness;

import rx.Subscription;

/**
 * @author wyh
 * @Description:
 * @creat 2017/12/27
 */
public class SubScriptionUtil {

    public static void unsubscribe(Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription == null) {
                return;
            }
            if (!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }

}
