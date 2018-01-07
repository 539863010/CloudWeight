package xm.cloudweight.utils.dao;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import xm.cloudweight.comm.BrocastFilter;

/**
 * @author wyh
 * @Description:
 * @creat 2018/1/7
 */
public class DbRefreshUtil {

    private static RefreshDbImageUploadReceiver mRefreshDbImageUploadReceiver;

    public static void refreshRegist(Context ctx, onDbRefreshListener listener) {
        mRefreshDbImageUploadReceiver = new RefreshDbImageUploadReceiver(listener);
        IntentFilter refreshDbImageUploadFilter = new IntentFilter(BrocastFilter.FILTER_REFRESH_HISTORY);
        LocalBroadcastManager.getInstance(ctx).registerReceiver(mRefreshDbImageUploadReceiver, refreshDbImageUploadFilter);

    }

    public static void refreshUnRegist(Context ctx) {
        if (mRefreshDbImageUploadReceiver != null) {
            LocalBroadcastManager.getInstance(ctx).unregisterReceiver(mRefreshDbImageUploadReceiver);
            mRefreshDbImageUploadReceiver = null;
        }
    }

    /**
     * 后台线程上传分拣接口成功后发送过来的广播
     */
    public static class RefreshDbImageUploadReceiver extends BroadcastReceiver {

        private final onDbRefreshListener listener;

        public RefreshDbImageUploadReceiver(onDbRefreshListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (listener != null) {
                listener.onRefresh();
            }
        }
    }

    public interface onDbRefreshListener {

        void onRefresh();

    }

}
