// ICameraService.aidl
package xm.cloudweight;

// Declare any non-default types here with import statements
import xm.cloudweight.OnScreenshotListener;
interface ICameraService {
    /**
           * Demonstrates some basic types that you can use as parameters
           * and return values in AIDL.
           */
          void setChannelName(String name);

          void screenshot(OnScreenshotListener listener);

          boolean isLight();
}
