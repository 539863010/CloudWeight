package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.service.purchase.PurchaseBillLine;

import xm.cloudweight.R;

/**
 * @author wyh
 * @Description: 验收详情
 * @creat 2017/10/29
 */
public class CheckInInfoPopWindow extends PopupWindow {

    private View mAnchor;
    private Context mContext;
    private TextView mTvCustomer;
    private EditText mEtInfoProduceArea;
    private EditText mEtInfoBatchNum;
    private EditText mEtInfoCredentialsNum;
    private EditText mEtInfoProduceDate;
    private EditText mEtInfoEffectiveDate;
    private EditText mEtInfoRemark;

    public CheckInInfoPopWindow(Context context, View anchor) {
        super(context);
        this.mAnchor = anchor;
        this.mContext = context;
        init();
    }

    private void init() {

        View v = LayoutInflater.from(mContext).inflate(R.layout.pop_check_in_info, null);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setContentView(v);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent)));
        //客户信息
        mTvCustomer = v.findViewById(R.id.tv_info_customer);
        //产地
        mEtInfoProduceArea = v.findViewById(R.id.et_info_produce_are);
        //批号
        mEtInfoBatchNum = v.findViewById(R.id.et_info_batch_num);
        //上市凭证号
        mEtInfoCredentialsNum = v.findViewById(R.id.et_info_credentials_num);
        //生产日期
        mEtInfoProduceDate = v.findViewById(R.id.et_info_produce_date);
        //有效日期
        mEtInfoEffectiveDate = v.findViewById(R.id.et_info_effective_date);
        //验收备注
        mEtInfoRemark = v.findViewById(R.id.et_info_remark);

    }

    public void show() {
        if (mAnchor == null) {
            throw new RuntimeException("must set anchor");
        }
        showAsDropDown(mAnchor, 0, 0);
    }

    public void setInfo(PurchaseBillLine purchaseBillLine) {
        if (purchaseBillLine == null) {
            mTvCustomer.setText("");
            mEtInfoProduceArea.setText("");
            mEtInfoBatchNum.setText("");
            mEtInfoCredentialsNum.setText("");
            mEtInfoProduceDate.setText("");
            mEtInfoEffectiveDate.setText("");
            mEtInfoRemark.setText("");
            return;
        }
        IdName customer = purchaseBillLine.getCustomer();
        mTvCustomer.setText(customer != null && !TextUtils.isEmpty(customer.getName()) ? customer.getName() : "");
    }

    /**
     * 产地
     */
    public String getProduceArea() {
        return mEtInfoProduceArea.getText().toString().trim();
    }

    /**
     * 批号
     */
    public String getBatchNum() {
        return mEtInfoBatchNum.getText().toString().trim();
    }

    /**
     * 上市凭证号
     */
    public String getCredentialsNum() {
        return mEtInfoCredentialsNum.getText().toString().trim();
    }

    /**
     * 有效日期
     */
    public String getProduceDate() {
        return mEtInfoProduceDate.getText().toString().trim();
    }

    /**
     * 生产日期
     */
    public String getEffectiveDate() {
        return mEtInfoEffectiveDate.getText().toString().trim();
    }

    /**
     * 验收备注
     */
    public String getRemark() {
        return mEtInfoRemark.getText().toString().trim();
    }
}
