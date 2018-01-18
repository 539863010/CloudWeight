// IRequestDataService.aidl
package xm.cloudweight;

// Declare any non-default types here with import statements
import xm.cloudweight.OnRequestDataListener;
interface IRequestDataService {
    /**
     * 对于非基本数据类型，也不是String和CharSequence类型的，
     * 需要有方向指示，包括in、out和inout，in表示由客户端设置，out表示由服务端设置，inout是两者均可设置。
     */
    void onGetDataListener(long type,inout Map params,OnRequestDataListener listener);

}
