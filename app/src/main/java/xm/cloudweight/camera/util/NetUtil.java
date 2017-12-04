package xm.cloudweight.camera.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by black on 2017/8/11.
 */

public class NetUtil {

    public static boolean isNetAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null
                && info.getType() != ConnectivityManager.TYPE_ETHERNET
                && info.isAvailable();
    }

    public static void setProcessDefaultNetwork(Context context, Integer type) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.Network[] networks = connectivityManager.getAllNetworks();
        if (type == null) {
            ConnectivityManager.setProcessDefaultNetwork(null);
            return;
        }
        for (android.net.Network network : networks) {
            NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
            if (networkInfo.getType() == type) {
                ConnectivityManager.setProcessDefaultNetwork(network);
                break;
            }
        }
    }

}
