package xm.cloudweight.utils.bussiness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;

/**
 * @author wyh
 * @Description:
 * @creat 2018/1/17
 */
public class RefreshMerchantHelper {

    private static final String ACTION_REFRESH_MERCHANT = "ACTION_REFRESH_MERCHANT";
    private static final String KEY_REFRESH_MERCHANT = "ACTION_REFRESH_MERCHANT";
    private RefreshMerchant mReceiver;

    public void regist(Context context, onRefreshMerchantListener mRefreshMerchant) {
        mReceiver = new RefreshMerchant(mRefreshMerchant);
        context.registerReceiver(mReceiver, new IntentFilter(ACTION_REFRESH_MERCHANT));
    }

    public void unregist(Context context) {
        if (context != null && mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public class RefreshMerchant extends BroadcastReceiver {

        private final onRefreshMerchantListener mRefreshMerchant;

        public RefreshMerchant(onRefreshMerchantListener refreshMerchant) {
            mRefreshMerchant = refreshMerchant;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mRefreshMerchant == null) {
                return;
            }
            String strMerchant = intent.getStringExtra(KEY_REFRESH_MERCHANT);
            if (!TextUtils.isEmpty(strMerchant)) {
                mRefreshMerchant.get(GsonUtil.getGson().fromJson(strMerchant, Merchant.class));
            } else {
                mRefreshMerchant.get(null);
            }
        }
    }

    public interface onRefreshMerchantListener {

        void get(Merchant merchant);

    }

    public static void send(Context context, String merchantString) {
        Intent intent = new Intent(ACTION_REFRESH_MERCHANT);
        intent.putExtra(KEY_REFRESH_MERCHANT, merchantString);
        context.sendBroadcast(intent);
    }

}
