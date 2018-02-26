package xm.cloudweight;

import android.app.DatePickerDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.fragment.InputFragment;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.KeyBoardUtils;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.onScanFinishListener;

/**
 * @author : wyh
 * @create : 2018/2/26
 * @des :  加工入库
 */
public class ProcessStorageActivity extends BaseActivity implements VideoFragment.OnInstrumentListener {

    private VideoFragment mVideoFragment;
    private InputFragment mInputFragment;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.et_finish_produce_label)
    ScanEditText mEtFinishProduceLabel;
    @BindView(R.id.iv_delete)
    ImageView mIvDelete;
    private EditText mEtProduceAccumulate;
    private EditText mEtWeightCurrent;
    private SearchAndFocusEditText mEtBucklesLeather;
    private SearchAndFocusEditText mEtDeductBad;
    private SearchAndFocusEditText mEtDeduceProduction;
    private SearchAndFocusEditText mEtStorageInPrice;

    @Override
    public String getBaseTitle() {
        return "加工入库";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_process_storage;
    }

    @Override
    protected void initContentView() {
        //请求焦点  解决监听EditText问题
        findViewById(R.id.activity_process_storage).requestFocus();
        mEtProduceAccumulate = (EditText) getEditText(R.id.ll_produce_accumulate, "累计产量");
        mEtProduceAccumulate.setEnabled(false);
        mEtProduceAccumulate.setBackground(null);
        mEtProduceAccumulate.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtWeightCurrent = (EditText) getEditText(R.id.ll_weight_current, "当前重量");
        mEtWeightCurrent.setEnabled(false);
        mEtWeightCurrent.setBackground(null);
        mEtWeightCurrent.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtBucklesLeather = (SearchAndFocusEditText) getEditText(R.id.ll_buckles_leather, "扣皮");
        mEtDeductBad = (SearchAndFocusEditText) getEditText(R.id.ll_deduct_bad, "不良");
        mEtDeduceProduction = (SearchAndFocusEditText) getEditText(R.id.ll_item_production, "产量");
        mEtStorageInPrice = (SearchAndFocusEditText) getEditText(R.id.ll_storage_in_price, "入库单价");
        findViewById(R.id.ll_storage_in_price).findViewById(R.id.tv_unit).setVisibility(View.INVISIBLE);

        //设置监听
        mEtFinishProduceLabel.setOnScanFinishListener(mLabelOnScanFinishListener);

        //初始化视频
        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        //禁止弹软键盘
        mInputFragment = InputFragment.newInstance();
        mInputFragment.setEditTexts(mEtBucklesLeather, mEtDeductBad, mEtDeduceProduction, mEtStorageInPrice);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mVideoFragment)
                .add(R.id.container, mInputFragment)
                .hide(mInputFragment)
                .show(mVideoFragment)
                .commitAllowingStateLoss();

        //        DbRefreshUtil.refreshRegist(this, new DbRefreshUtil.onDbRefreshListener() {
        //            @Override
        //            public void onRefresh() {
        //                if (mHistoryCheckInPopWindow != null && mHistoryCheckInPopWindow.isShowing()) {
        //                    refreshHistoryList();
        //                }
        //            }
        //        });
    }

    @Override
    protected void loadDate() {
        //设置当前日期
        String currentData = DateUtils.StringData();
        mBtnDate.setText(currentData);

    }

    @OnClick({R.id.btn_date, R.id.btn_clear_zero})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_date:
                //显示时间选择框
                String dateStr = mBtnDate.getText().toString().trim();
                DatePickerDialogUtil.show(getContext(), dateStr, mOnDateSetListener);
                break;
            case R.id.btn_clear_zero:
                clearToZero();
                break;
            default:
                break;
        }
    }

    private onScanFinishListener mLabelOnScanFinishListener = new onScanFinishListener() {
        @Override
        public void onScanFinish(String key) {
            if (!TextUtils.isEmpty(key)) {
                scanLabel(key);
            }
            setIvDeleteVis(key);
        }

        private void setIvDeleteVis(String key) {
            if (!TextUtils.isEmpty(key)) {
                if (mIvDelete.getVisibility() != View.VISIBLE) {
                    mIvDelete.setVisibility(View.VISIBLE);
                }
            } else {
                mIvDelete.setVisibility(View.GONE);
            }
        }
    };

    private void scanLabel(String purchaseBatchScan) {
        showLoadingDialog(false);
        KeyBoardUtils.closeKeybord(mEtFinishProduceLabel, getContext());
        //        if (mListAll.size() == 0) {
        //            ToastUtil.showShortToast(getContext(), "暂无数据");
        //            return;
        //        }
        //        for (PurchaseData data : mListAll) {
        //            String purchaseBatch = data.getPurchaseBillLine().getPurchaseBatch();
        //            if (purchaseBatchScan.equals(purchaseBatch)) {
        //                mPurchaseData = data;
        //                mPurchaseBillLine = mPurchaseData.getPurchaseBillLine();
        //                setPurchaseBillLineInfo();
        //                mEtPurChaseLabel.setText("");
        //                dismissLoadingDialog();
        //                break;
        //            }
        //        }
        dismissLoadingDialog();
    }

    /**
     * 日期选择监听
     */
    private DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            String monthStr = ((month + 1) < 10) ? "0" + (month + 1) : (month + 1) + "";
            String dayStr = (dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth + "";
            String date = year + "-" + monthStr + "-" + dayStr;
            mBtnDate.setText(date);
            //请求数据
            showLoadingDialog(false);

            //            clearGoodsList();

            //            queryPurchaseData();
        }
    };

    @Override
    public void receive(Instrument.InsData data) {

    }

    private View getEditText(int viewGroupId, String title) {
        View viewGroup = findViewById(viewGroupId);
        ((TextView) viewGroup.findViewById(R.id.tv_title)).setText(title);
        return viewGroup.findViewById(R.id.ed_content);
    }
}
