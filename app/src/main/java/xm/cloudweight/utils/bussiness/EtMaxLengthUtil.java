package xm.cloudweight.utils.bussiness;

import android.text.InputFilter;

import xm.cloudweight.comm.Common;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/21
 */
public class EtMaxLengthUtil {

    public static InputFilter[] getFilter() {
        InputFilter[] filters =  {new InputFilter.LengthFilter(Common.BasketLength)};
        return filters;
    }

}
