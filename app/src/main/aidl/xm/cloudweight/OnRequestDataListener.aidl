// OnRequestDataListener.aidl
package xm.cloudweight;

// Declare any non-default types here with import statements

interface OnRequestDataListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

   void onReceive(long type);

   void onError(long type,String message);


}
