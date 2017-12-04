// OnScreenshotListener.aidl
package xm.cloudweight;

// Declare any non-default types here with import statements

interface OnScreenshotListener {
    /**
          * Demonstrates some basic types that you can use as parameters
          * and return values in AIDL.
          */

         void start();

         void success(String msg);

         void error(String msg);
}
