package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.service.sort.SortOutData;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import xm.cloudweight.R;
import xm.cloudweight.bean.BeanPrinter;
import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/12
 */
public class PrinterView extends LinearLayout {

    private TextView mPvName;
    private TextView mPvCount;
    private TextView mPvCustomer;
    private TextView mPvCode;
    private TextView mPvCustomerDept;

    public PrinterView(Context context) {
        super(context);
        init();
    }

    public PrinterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrinterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_printer, this);
        mPvName = v.findViewById(R.id.pv_name);
        mPvCount = v.findViewById(R.id.pv_count);
        mPvCustomer = v.findViewById(R.id.pv_customer);
        mPvCustomerDept = v.findViewById(R.id.pv_customer_dept);
        mPvCode = v.findViewById(R.id.pv_code);
    }

    public void set(BeanPrinter bean) {
        if (bean != null) {
            mPvName.setText(check(bean.getGoodsName()));
            mPvCount.setText(check(bean.getCount()));
            mPvCustomer.setText(check(bean.getCustomer()));
            String department = bean.getDepartment();
            if ( !TextUtils.isEmpty(department)) {
                mPvCustomerDept.setVisibility(View.VISIBLE);
                mPvCustomerDept.setText(department);
            } else {
                mPvCustomerDept.setVisibility(View.GONE);
            }
            mPvCode.setText(Common.MACHINE_NUM.concat(DateUtils.getTime3(new Date())));
        }
    }

    private String check(String content) {
        return TextUtils.isEmpty(content) ? "" : content;
    }

    public String getPath() {
        setDrawingCacheEnabled(true);
        Bitmap tBitmap = getDrawingCache();
        // 拷贝图片，否则在setDrawingCacheEnabled(false)以后该图片会被释放掉
        tBitmap = tBitmap.createBitmap(tBitmap);
        setDrawingCacheEnabled(false);

        String filePath = Environment.getExternalStorageDirectory().getPath() + "/printer.jpg";
        File file = new File(filePath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            tBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }
}
