// IRequestDataService.aidl
package xm.cloudweight;

// Declare any non-default types here with import statements
import xm.cloudweight.OnRequestDataListener;
interface IRequestDataService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onGetDataListener(int type,String params,OnRequestDataListener listener);

}
