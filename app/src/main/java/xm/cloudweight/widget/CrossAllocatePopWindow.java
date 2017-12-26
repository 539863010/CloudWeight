package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.xmzynt.storm.service.wms.warehouse.Warehouse;

import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.utils.bussiness.LocalSpUtil;

/**
 * @author wyh
 * @Description: 验收详情
 * @creat 2017/10/29
 */
public class CrossAllocatePopWindow extends PopupWindow {

    private View mAnchor;
    private Context mContext;

    public CrossAllocatePopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View v = LayoutInflater.from(mContext).inflate(R.layout.pop_cross_allocate, null);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        setContentView(v);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(false);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));

        v.findViewById(R.id.btn_cross_allocate_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        v.findViewById(R.id.btn_cross_allocate_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        DataSpinner<Warehouse> spCrossAllocate = v.findViewById(R.id.sp_corss_allocate_warehouse_in);
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(mContext);
        if (listWareHouse != null) {
            spCrossAllocate.setList(listWareHouse);
        }
    }

    public void show() {
        if (mAnchor == null) {
            throw new RuntimeException("must set anchor");
        }
        showAsDropDown(mAnchor, 0, 0);
    }

}
