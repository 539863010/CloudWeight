package xm.cloudweight.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.xmzynt.storm.util.GsonUtil;

import xm.cloudweight.R;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.impl.OnDeleteListener;

public class CancelDialog extends Dialog implements View.OnClickListener {

    private final OnDeleteListener mOnDeleteListener;
    private TextView mTvCancelMsg;
    private DbImageUpload mDbImageUpload;
    private Context mContext;

    public CancelDialog(Context context, OnDeleteListener listener) {
        super(context, R.style.dialog);
        mContext = context;
        mOnDeleteListener = listener;
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cancel_sort_out, null);
        setContentView(view);
        mTvCancelMsg = view.findViewById(R.id.tv_cancel_msg);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_sure).setOnClickListener(this);
    }

    public void showCancelDialog(DbImageUpload dbImageUpload, String operateType) {
        mDbImageUpload = dbImageUpload;
        CustomSortOutData data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), CustomSortOutData.class);
        String goodsName = data.getGoods().getName();
        String msg = mContext.getString(R.string.cancel_msg, goodsName, operateType);
        mTvCancelMsg.setText(msg);
        show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_sure:
                dismiss();
                if (mOnDeleteListener != null) {
                    mOnDeleteListener.delete(mDbImageUpload);
                }
                break;
            default:
                break;
        }
    }

}