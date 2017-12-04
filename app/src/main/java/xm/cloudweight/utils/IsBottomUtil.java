package xm.cloudweight.utils;

import android.view.View;
import android.widget.GridView;
import android.widget.ScrollView;

/**
 * @author wyh
 * @Description:
 * @creat 2017/12/4
 */
public class IsBottomUtil {

    public static boolean isBottom(GridView gv) {
        int lastPosition = gv.getLastVisiblePosition();
        int count = gv.getCount();
        int childCount = gv.getChildCount();
        View lastVisiableView = gv.getChildAt(childCount - 1);
        if (childCount == count) {
            return true;
        }
        if (lastVisiableView != null) {
            if (lastPosition == count - 1 && lastVisiableView.getBottom() + gv.getListPaddingBottom() == gv.getHeight()) {
                return true;
            }
        }
        return false;
    }

//    private boolean isBottom(ScrollView scrollView) {
//        if (scrollView.getScrollY() == 0) {
//            return true;
//        }
//        return false;
//    }

    private boolean isBottom(ScrollView sc) {
        int scrollY = sc.getScrollY();
        int height = sc.getHeight();
        View view = sc.getChildAt(0);
        if (view == null) {
            return true;
        } else {
            if ((scrollY + height) >= view.getMeasuredHeight()) {//滑到了底部
                return true;
            }
        }
        return false;
    }
}
