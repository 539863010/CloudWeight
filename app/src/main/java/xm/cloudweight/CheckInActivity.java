package xm.cloudweight;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.purchase.PurchaseBillLine;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.service.wms.stockin.StockInType;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.impl.CheckInImpl;
import xm.cloudweight.presenter.CheckInPresenter;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.KeyBoardUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.CrossAllocateUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.PrinterInventory;
import xm.cloudweight.utils.bussiness.PrinterSortOut;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.DbRefreshUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.HistoryCheckInPopWindow;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

/**
 * @author wyh
 * @description: 验收
 * @create 2017/10/30
 */
public class CheckInActivity extends BaseActivity implements
        CheckInImpl.OnQueryPurchaseDataListener,
        CheckInImpl.OnCancelStockInListener
        , Spinner.OnItemSelectedListener, VideoFragment.OnInstrumentListener, HistoryCheckInPopWindow.OnDeleteListener {

    @BindView(R.id.sp_ware_house)
    DataSpinner<Warehouse> mSpWareHouse;
    @BindView(R.id.sp_purchaseBill)
    DataSpinner<String> mSpSupplier;
    @BindView(R.id.lv_line)
    ListView mLvPurchase;
    @BindView(R.id.tv_info_goods)
    TextView mTvInfoGoods;
    @BindView(R.id.tv_info_purchase_num)
    TextView mTvInfoPurchaseNum;
    @BindView(R.id.tv_info_purchase_remark)
    TextView mTvInfoPurchaseRemark;
    @BindView(R.id.et_key_search)
    SearchAndFocusEditText mEtKeySearch;
    @BindView(R.id.et_purchase_label)
    ScanEditText mEtPurChaseLabel;
    @BindView(R.id.iv_delete)
    ImageView mIvDelete;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.btn_stock_in)
    Button mBtnStockIn;
    @BindView(R.id.btn_stock_cross)
    Button mBtnStockCross;
    @BindView(R.id.btn_cross_allocate)
    Button mBtnCrossAllocate;
    @BindView(R.id.iv_print_label_add)
    ImageView mIvPrintLabelAdd;
    @BindView(R.id.iv_print_label_sub)
    ImageView mIvPrintLabelSub;
    @BindView(R.id.et_print_label_count)
    EditText mEtPrintLabelCount;
    private List<PurchaseData> mListShow = new ArrayList<>();
    private List<PurchaseData> mListAll = new ArrayList<>();
    private PurchaseBillLineAdapter mAdapterPurchase;
    private PurchaseData mPurchaseData;
    private PurchaseBillLine mPurchaseBillLine;
    private EditText mEtWeightCurrent;
    private SearchAndFocusEditText mEtWeightAccumulate;
    private SearchAndFocusEditText mEtBucklesLeather;
    private SearchAndFocusEditText mEtDeductWeight;
    private EditText mEtNumWarehousing;
    private TextView mTvNumWarehousingUnit;
    /**
     * 记录累计重量(已入库数 的 多次累计)
     */
    private Map<String, String> mMapAccumulate = new HashMap<>();
    private TextView mTvWeightAccumulateUnit;
    private static final int TYPE_STORE_IN = 1;
    private static final int TYPE_CROSS = 2;
    private static final int TYPE_CROSS_ALLOCATE = 3;
    private int mIntTypeUpload;
    private DBManager mDBManager;
    private ImageView mIvSub;
    private ImageView mIvAdd;
    private VideoFragment mVideoFragment;
    private EditText mEtWareHourseIn;
    private AlertDialog mCrossAllocateDialog;
    private Warehouse mWareHouseTo;
    private HistoryCheckInPopWindow mHistoryCheckInPopWindow;
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private DbImageUpload mDbImageUpload;
    private boolean hasCancelCheckIn;
    private onScanFinishListener mOnScanFinishListener;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_check_in;
    }

    @Override
    protected void initContentView() {
        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mVideoFragment).commitAllowingStateLoss();
        //请求焦点  解决监听EditText问题
        findViewById(R.id.activity_check_in).requestFocus();
        mEtWeightCurrent = (EditText) getEditText(R.id.ll_weight_current, "当前重量");
        mEtWeightCurrent.setEnabled(false);
        mEtWeightCurrent.setBackground(null);
        mEtWeightCurrent.setTextColor(getResources().getColor(R.color.red));
        mEtWeightCurrent.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtWeightAccumulate = (SearchAndFocusEditText) getEditText(R.id.ll_weight_accumulate, "累计重量");
        mTvWeightAccumulateUnit = ((TextView) findViewById(R.id.ll_weight_accumulate).findViewById(R.id.tv_unit));
        mEtWeightAccumulate.setEnabled(false);
        mEtWeightAccumulate.setBackground(null);
        mEtWeightAccumulate.setTextColor(getResources().getColor(R.color.blue));
        mEtWeightCurrent.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtBucklesLeather = (SearchAndFocusEditText) getEditText(R.id.ll_buckles_leather, "扣皮");
        mEtDeductWeight = (SearchAndFocusEditText) getEditText(R.id.ll_deduct_weight, "扣重");
        mEtNumWarehousing = (EditText) getEditText(R.id.ll_num_warehousing, "入库数量");
        mTvNumWarehousingUnit = ((TextView) findViewById(R.id.ll_num_warehousing).findViewById(R.id.tv_unit));
        mIvSub = findViewById(R.id.ll_num_warehousing).findViewById(R.id.img_sub);
        mIvAdd = findViewById(R.id.ll_num_warehousing).findViewById(R.id.img_add);
        TextView tvWareHouseInUnit = (TextView) findViewById(R.id.ll_warehourse_in).findViewById(R.id.tv_unit);
        tvWareHouseInUnit.setText("元");
        tvWareHouseInUnit.setVisibility(View.INVISIBLE);
        mEtWareHourseIn = (EditText) getEditText(R.id.ll_warehourse_in, "入库仓库");
        mEtWareHourseIn.setEnabled(false);
        mAdapterPurchase = new PurchaseBillLineAdapter(this, mListShow);
        mLvPurchase.setAdapter(mAdapterPurchase);
        setViewListener();
    }

    private void setViewListener() {
        mSpWareHouse.setCustomItemSelectedListener(this);
        mSpSupplier.setCustomItemSelectedListener(this);
        mLvPurchase.setOnItemClickListener(mOnItemPurchaseClickListener);
        mEtPurChaseLabel.setOnScanFinishListener(mLabelOnScanFinishListener);
        mEtWeightCurrent.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String strCurrentWeight = s.toString().trim();
                if (TextUtils.isEmpty(strCurrentWeight)) {
                    return;
                }
                BigDecimal currentWeight = new BigDecimal(strCurrentWeight);
                String strLeather = mEtBucklesLeather.getText().toString().trim();
                BigDecimal leather = new BigDecimal(!TextUtils.isEmpty(strLeather) ? strLeather : "0");
                String strDeduct = mEtDeductWeight.getText().toString().trim();
                BigDecimal deduct = new BigDecimal(!TextUtils.isEmpty(strDeduct) ? strDeduct : "0");
                BigDecimal weightCoefficient = new BigDecimal(1);
                if (isWeight()) {
                    weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
                }
                BigDecimal finallyNum = currentWeight.subtract(leather).subtract(deduct).divide(weightCoefficient, RoundingMode.HALF_EVEN);
                mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(finallyNum));
            }
        });
        mIvSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCount()) {
                    BigDecimal count = getEtBigDecimal(mEtNumWarehousing);
                    if (count.doubleValue() > 1) {
                        mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(count.subtract(new BigDecimal(1))));
                    } else {
                        mEtNumWarehousing.setText("0.00");
                    }
                }
            }
        });
        mIvAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCount()) {
                    BigDecimal count = getEtBigDecimal(mEtNumWarehousing);
                    mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(count.add(new BigDecimal(1))));
                }
            }
        });
        mEtWeightAccumulate.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
        mEtBucklesLeather.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
        mEtDeductWeight.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
        DbRefreshUtil.refreshRegist(this, new DbRefreshUtil.onDbRefreshListener() {
            @Override
            public void onRefresh() {
                if (mHistoryCheckInPopWindow != null && mHistoryCheckInPopWindow.isShowing()) {
                    refreshHistoryList();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbRefreshUtil.refreshUnRegist(this);
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
        KeyBoardUtils.closeKeybord(mEtPurChaseLabel, getContext());
        if (mListAll.size() == 0) {
            ToastUtil.showShortToast(getContext(), "暂无数据");
            return;
        }
        for (PurchaseData data : mListAll) {
            String purchaseBatch = data.getPurchaseBillLine().getPurchaseBatch();
            if (purchaseBatchScan.equals(purchaseBatch)) {
                mPurchaseData = data;
                mPurchaseBillLine = mPurchaseData.getPurchaseBillLine();
                setPurchaseBillLineInfo();
                mEtPurChaseLabel.setText("");
                dismissLoadingDialog();
                break;
            }
        }
        dismissLoadingDialog();
    }

    /**
     * 当前选择为数量单位
     */
    private boolean isCount() {
        return mPurchaseData != null &&
                (mPurchaseBillLine != null && mPurchaseBillLine.getWeightCoefficient() == null);
    }

    /**
     * 当前选择为重量单位
     */
    private boolean isWeight() {
        return mPurchaseData != null &&
                (mPurchaseBillLine != null
                        && mPurchaseBillLine.getWeightCoefficient() != null
                        && mPurchaseBillLine.getWeightCoefficient().doubleValue() != 0);
    }

    /**
     * 文本框监听
     */
    private onInputFinishListener mOnInputFinishListenerSetStoreInNum = new onInputFinishListener() {
        @Override
        public void onFinish(String key) {
            if (mPurchaseData == null) {
                return;
            }
            setStoreInNum();
        }
    };

    /**
     * 设置入库数量
     */
    private void setStoreInNum() {
        String strCurrentWeight = mEtWeightCurrent.getText().toString().trim();
        BigDecimal currentWeight = new BigDecimal(!TextUtils.isEmpty(strCurrentWeight) ? strCurrentWeight : "0");
        String strAccumulate = mEtWeightAccumulate.getText().toString().trim();
        BigDecimal accumulate = new BigDecimal(!TextUtils.isEmpty(strAccumulate) ? strAccumulate : "0");
        String strLeather = mEtBucklesLeather.getText().toString().trim();
        BigDecimal leather = new BigDecimal(!TextUtils.isEmpty(strLeather) ? strLeather : "0");
        String strDeduct = mEtDeductWeight.getText().toString().trim();
        BigDecimal deduct = new BigDecimal(!TextUtils.isEmpty(strDeduct) ? strDeduct : "0");
        BigDecimal amount;
        if (accumulate.doubleValue() == 0) {
            //当前没累计的话，设置当前重量为累计重量
            amount = currentWeight;
        } else {
            //当前有累计的话，取累计值
            amount = accumulate;
        }
        BigDecimal weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
        BigDecimal finallyCount = amount.subtract(leather).subtract(deduct).divide(weightCoefficient, ROUND_HALF_EVEN);
        if (finallyCount.doubleValue() < 0) {
            mEtNumWarehousing.setText("0.00");
        } else {
            String num = BigDecimalUtil.toScaleStr(finallyCount);
            mEtNumWarehousing.setText(num);
        }
    }

    @Override
    protected void loadDate() {
        //设置当前日期
        String currentData = DateUtils.StringData();
        mBtnDate.setText(currentData);

        showLoadingDialog(true);
        getWareHouseList();
        CheckInPresenter.queryPurchaseData(this, 0, 0, 0, currentData);
    }

    private void getWareHouseList() {
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(this);
        if (listWareHouse == null) {
            listWareHouse = new ArrayList<>();
        }
        Warehouse warehouse = new Warehouse();
        warehouse.setName("全部");
        listWareHouse.add(0, warehouse);
        if (listWareHouse.size() == 1) {
            ToastUtil.showShortToast(this, "未获取到仓库列表信息");
        } else {
            mSpWareHouse.setList(listWareHouse);
        }
    }

    @Override
    public String getBaseTitle() {
        return "验收";
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_ware_house:
            case R.id.sp_purchaseBill:
                showLoadingDialog(false);
                filterList();
                dismissLoadingDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @OnClick({R.id.btn_stock_in, R.id.btn_stock_cross, R.id.btn_date, R.id.btn_clear_zero,
            R.id.iv_sort_out_search, R.id.btn_cross_allocate, R.id.iv_print_label_sub,
            R.id.iv_print_label_add, R.id.btn_check_in_history
            , R.id.iv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_date:
                //显示时间选择框
                String dateStr = mBtnDate.getText().toString().trim();
                DatePickerDialogUtil.show(getContext(), dateStr, mOnDateSetListener);
                break;
            case R.id.btn_stock_in:
                if (check()) return;
                mIntTypeUpload = TYPE_STORE_IN;
                setBtnEnable(mBtnCrossAllocate, false, mBtnStockCross, false, mBtnStockIn, false);
                showLoadingDialog(false);
                if (mVideoFragment.isLight()) {
                    mVideoFragment.screenshot(mHandlerShotPic);
                } else {
                    shotResult(null);
                }
                break;
            case R.id.btn_stock_cross:
                if (check()) return;
                if (mPurchaseData != null && mPurchaseData.getPurchaseBillLine() != null) {
                    PurchaseBillLine purchaseBillLine = mPurchaseData.getPurchaseBillLine();
                    if (!TextUtils.isEmpty(purchaseBillLine.getSourcePlanUuid())
                            && !TextUtils.isEmpty(purchaseBillLine.getGoodsOrderBillNumber())) {
                        mIntTypeUpload = TYPE_CROSS;
                        setBtnEnable(mBtnCrossAllocate, false, mBtnStockCross, false, mBtnStockIn, false);
                        showLoadingDialog(false);
                        if (mVideoFragment.isLight()) {
                            mVideoFragment.screenshot(mHandlerShotPic);
                        } else {
                            shotResult(null);
                        }
                    } else {
                        ToastUtil.showShortToast(getContext(), "无客户信息");
                    }
                }
                break;
            case R.id.btn_cross_allocate:
                if (check()) return;
                if (mPurchaseData == null || mPurchaseBillLine == null) {
                    ToastUtil.showShortToast(getContext(), "请先选择商品");
                    return;
                }
                showCrossAllocateDialog();
                break;
            case R.id.iv_sort_out_search:
                keySearch();
                break;
            case R.id.iv_print_label_add: {
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                count++;
                mEtPrintLabelCount.setText(String.valueOf(count));
            }
            break;
            case R.id.iv_print_label_sub: {
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                if (count > 1) {
                    count--;
                    mEtPrintLabelCount.setText(String.valueOf(count));
                }
            }
            break;
            case R.id.btn_clear_zero:
                clearToZero();
                break;
            case R.id.btn_check_in_history:
                showHistory();
                break;
            case R.id.iv_delete:
                String label = mEtPurChaseLabel.getText().toString().trim();
                if (!TextUtils.isEmpty(label)) {
                    mEtPurChaseLabel.setOnScanFinishListener(null);
                    mEtPurChaseLabel.setText("");
                    mEtPurChaseLabel.setOnScanFinishListener(mLabelOnScanFinishListener);
                }
                break;
            default:
                break;
        }
    }

    private void showHistory() {
        if (mHistoryCheckInPopWindow == null) {
            mHistoryCheckInPopWindow = new HistoryCheckInPopWindow(getContext(), mSpWareHouse);
            mHistoryCheckInPopWindow.setOnDeleteListener(this);
            mHistoryCheckInPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (hasCancelCheckIn) {
                        mAdapterPurchase.notifyDataSetChanged();
                    }
                    hasCancelCheckIn = false;
                }
            });
        }
        refreshHistoryList();
    }

    @Override
    public void delete(DbImageUpload data) {
        mDbImageUpload = data;
        int type = data.getType();
        if (type == Common.DbType.TYPE_ChECK_IN_STORE_IN || type == Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE) {
            requestCancel(type, data.getStockInUuid());
        } else if (type == Common.DbType.TYPE_ChECK_IN_CROSS_OUT) {
            requestCancel(type, data.getStockOutUuid());
        }
    }

    private void requestCancel(int type, String uuid) {
        if (!TextUtils.isEmpty(uuid)) {
            CheckInPresenter.cancelStockIn(getActivity(), uuid, type);
        } else {
            onCancelSuccess();
        }
    }

    private void onCancelSuccess() {
        //先刷新后删除数据库
        refreshCancelCheckIn();
        getDbManager().deleteDbImageUpload(mDbImageUpload);
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销成功");
        hasCancelCheckIn = true;
        dismissLoadingDialog();
        mDbImageUpload = null;
    }

    private void refreshCancelCheckIn() {
        String date = mDbImageUpload.getDate();
        String currentSelectedDate = mBtnDate.getText().toString().trim();
        if (!date.equals(currentSelectedDate)) {
            return;
        }
        StockInRecord stockInRecord = GsonUtil.getGson().fromJson(mDbImageUpload.getLine(), StockInRecord.class);
        String cancelUuid = stockInRecord.getSourceBillLineUuid();
        for (PurchaseData data : mListAll) {
            PurchaseBillLine purchaseBillLine = data.getPurchaseBillLine();
            if (cancelUuid.equals(purchaseBillLine.getUuid())) {
                BigDecimal quantity = stockInRecord.getQuantity();
                BigDecimal hasStockInQty = purchaseBillLine.getHasStockInQty();
                purchaseBillLine.setHasStockInQty(hasStockInQty.subtract(quantity));
                break;
            }
        }
    }

    /**
     * 刷新历史列表
     */
    private void refreshHistoryList() {
        //获取数据库当天的数据
        String date = mBtnDate.getText().toString().trim();
        List<DbImageUpload> daoList = getDbManager().getDbListCheckInHistory(date);
        Collections.reverse(daoList);
        mListHistory.clear();
        mListHistory.addAll(daoList);
        mHistoryCheckInPopWindow.show();
        mHistoryCheckInPopWindow.notify(mListHistory);
    }

    @Override
    public void onCancelStockInSuccess(String result) {
        onCancelSuccess();
    }

    @Override
    public void onCancelStockInFailed(String message) {
        dismissLoadingDialog();
        mDbImageUpload = null;
        ToastUtil.showShortToast(getContext(), message);
    }

    private boolean check() {
        if (Integer.parseInt(mEtPrintLabelCount.getText().toString().trim()) < 1) {
            ToastUtil.showShortToast(getContext(), "标签数不能小于1");
            return true;
        }
        if (TextUtils.isEmpty(mEtNumWarehousing.getText().toString().trim())) {
            ToastUtil.showShortToast(getContext(), "入库数量不能为空");
            return true;
        }
        return false;
    }

    private void showCrossAllocateDialog() {
        if (mCrossAllocateDialog == null) {
            mCrossAllocateDialog = CrossAllocateUtil.create(getActivity(), new CrossAllocateUtil.onCrossAllocateOperationInterface() {
                @Override
                public void cancel() {
                    mCrossAllocateDialog.dismiss();
                }

                @Override
                public void sure(Warehouse warehouse) {
                    UCN ucn = mPurchaseData.getWarehouse();
                    if (warehouse.getName().equals(ucn.getName()) && warehouse.getCode().equals(ucn.getCode())) {
                        ToastUtil.showShortToast(getContext(), "入库仓库与越库调拨仓库相同");
                        return;
                    }
                    mCrossAllocateDialog.dismiss();
                    mIntTypeUpload = TYPE_CROSS_ALLOCATE;
                    mWareHouseTo = warehouse;
                    setBtnEnable(mBtnCrossAllocate, false, mBtnStockCross, false, mBtnStockIn, false);
                    showLoadingDialog(false);
                    if (mVideoFragment.isLight()) {
                        mVideoFragment.screenshot(mHandlerShotPic);
                    } else {
                        shotResult(null);
                    }
                }
            });
        }
        CrossAllocateUtil.show(mCrossAllocateDialog);
    }

    /**
     * 关键字搜索
     */
    private void keySearch() {
        showLoadingDialog(false);
        mSpWareHouse.setCustomItemSelectedListener(null);
        mSpSupplier.setCustomItemSelectedListener(null);
        mSpWareHouse.setSelection(0);
        mSpSupplier.setSelection(0);
        filterList();
        mSpWareHouse.setCustomItemSelectedListener(this);
        mSpSupplier.setCustomItemSelectedListener(this);
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

            clearGoodsList();
            CheckInPresenter.queryPurchaseData(getActivity(), 0, 0, 0, date);
        }
    };

    /**
     * 商品列表item点击
     */
    private AdapterView.OnItemClickListener mOnItemPurchaseClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mPurchaseData = mListShow.get(i);
            mPurchaseBillLine = mPurchaseData.getPurchaseBillLine();
            setPurchaseBillLineInfo();
        }
    };

    /**
     * 填充信息
     */
    private void setPurchaseBillLineInfo() {
        clearText();
        //设置累计重量
        BigDecimal weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
        if (mMapAccumulate.containsKey(mPurchaseBillLine.getUuid())) {
            mEtWeightAccumulate.setText(mMapAccumulate.get(mPurchaseBillLine.getUuid()));
        } else {
            mEtWeightAccumulate.setText("");
        }
        if (weightCoefficient != null) {
            mTvWeightAccumulateUnit.setText("kg");
            mEtBucklesLeather.setEnabled(true);
            mEtDeductWeight.setEnabled(true);
            mEtNumWarehousing.setEnabled(false);
            mIvAdd.setVisibility(View.GONE);
            mIvSub.setVisibility(View.GONE);
        } else {
            mTvWeightAccumulateUnit.setText(mPurchaseBillLine.getGoodsUnit().getName());
            mEtBucklesLeather.setEnabled(false);
            mEtDeductWeight.setEnabled(false);
            mEtNumWarehousing.setEnabled(true);
            mIvAdd.setVisibility(View.VISIBLE);
            mIvSub.setVisibility(View.VISIBLE);
        }
        //商品名
        UCN goods = mPurchaseBillLine.getGoods();
        setText(mTvInfoGoods, (goods != null && !TextUtils.isEmpty(goods.getName())) ? goods.getName() : "");
        //采购数+规格
        IdName goodsUnit = mPurchaseBillLine.getGoodsUnit();
        String unit = (goodsUnit != null && !TextUtils.isEmpty(goodsUnit.getName()) ? goodsUnit.getName() : "");

        BigDecimal purchaseQty = mPurchaseBillLine.getPurchaseQty();
        String strPurchaseQty = BigDecimalUtil.toScaleStr(purchaseQty);
        setText(mTvInfoPurchaseNum, strPurchaseQty + (unit));
        //采购备注
        setText(mTvInfoPurchaseRemark, "采购备注：" + (mPurchaseBillLine.getRemark() == null ? "" : mPurchaseBillLine.getRemark()));
        //设置入库数量单位
        mTvNumWarehousingUnit.setText(unit);
        // 入库数量  (入库数量默认等于采购数量)
        String currentWeight = mEtWeightCurrent.getText().toString().trim();
        if (mPurchaseBillLine.getWeightCoefficient() != null && !TextUtils.isEmpty(currentWeight)) {
            // 设置为当前体重秤上面的数值
            setEditText(mEtNumWarehousing, currentWeight);
        } else {
            setEditText(mEtNumWarehousing, strPurchaseQty);
        }
        //设置入库仓库
        UCN warehouse = mPurchaseData.getWarehouse();
        if (warehouse != null && !TextUtils.isEmpty(warehouse.getName())) {
            mEtWareHourseIn.setText(warehouse.getName());
        }
    }

    /**
     * 清空商品列表
     */
    private void clearGoodsList() {
        if (mListShow.size() > 0) {
            mListShow.clear();
        }
        mAdapterPurchase.notifyDataSetChanged();
    }

    /**
     * 清除商品信息
     */
    private void clearText() {
        mTvInfoGoods.setText("");
        mTvInfoPurchaseNum.setText("");
        mTvNumWarehousingUnit.setText("kg");
        mTvInfoPurchaseRemark.setText("采购备注：");
        setEtWeightCurrent("");
        mEtWeightAccumulate.setText("");
        mTvWeightAccumulateUnit.setText("kg");
        mEtBucklesLeather.setText("");
        mEtDeductWeight.setText("");
        mEtNumWarehousing.setText("");
        mEtWareHourseIn.setText("");
    }

    /**
     * 拍照Handler
     */
    private Handler mHandlerShotPic = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:

                    break;
                case 2:
                    String path = msg.getData().getString("path", "");
                    shotResult(path);
                    break;
                case 3:
                    shotResult(null);
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private void shotResult(String imagePath) {
        StockInRecord record = prepareConfig(imagePath);
        String date = mBtnDate.getText().toString().trim();
        if (mIntTypeUpload == TYPE_STORE_IN) {
            //入库
            DbImageUpload db = new DbImageUpload();
            db.setDate(date);
            db.setOperatime(DateUtils.getTime2(new Date()));
            db.setImagePath(imagePath);
            db.setLine(GsonUtil.getGson().toJson(record));
            db.setType(Common.DbType.TYPE_ChECK_IN_STORE_IN);
            getDbManager().insertDbImageUpload(db);
            ToastUtil.showShortToast(getContext(), "入库成功");
        } else if (mIntTypeUpload == TYPE_CROSS) {
            //越库
            DbImageUpload db = new DbImageUpload();
            db.setDate(date);
            db.setOperatime(DateUtils.getTime2(new Date()));
            db.setImagePath(imagePath);
            db.setLine(GsonUtil.getGson().toJson(record));
            db.setType(Common.DbType.TYPE_ChECK_IN_CROSS_OUT);
            getDbManager().insertDbImageUpload(db);
            ToastUtil.showShortToast(getContext(), "越库成功");
        } else if (mIntTypeUpload == TYPE_CROSS_ALLOCATE) {
            DbImageUpload db = new DbImageUpload();
            db.setDate(date);
            db.setOperatime(DateUtils.getTime2(new Date()));
            db.setImagePath(imagePath);
            db.setLine(GsonUtil.getGson().toJson(record));
            db.setType(Common.DbType.TYPE_ChECK_IN_CROSS_ALLCOCATE);
            getDbManager().insertDbImageUpload(db);
            ToastUtil.showShortToast(getContext(), "越库调拨成功");
        }
        refreshSuccessData(record);
        setBtnEnable(mBtnCrossAllocate, true, mBtnStockCross, true, mBtnStockIn, true);
        dismissLoadingDialog();
    }

    private void setBtnEnable(Button b1, boolean openB1,
                              Button b2, boolean openB2,
                              Button b3, boolean openB3) {
        b1.setEnabled(openB1);
        b2.setEnabled(openB2);
        b3.setEnabled(openB3);
    }

    /**
     * 创建入库、越库 请求体
     */
    private StockInRecord prepareConfig(String imagePath) {
        if (mPurchaseData != null && mPurchaseBillLine != null) {
            StockInRecord sir = new StockInRecord();
            UCN warehouse = mPurchaseData.getWarehouse();
            sir.setWarehouse(new UCN(warehouse.getUuid(), warehouse.getCode(), warehouse.getName()));
            sir.setGoods(mPurchaseBillLine.getGoods());
            //保存图片
            if (!TextUtils.isEmpty(imagePath)) {
                sir.setImages(Arrays.asList(imagePath));
            }
            //规格
            sir.setGoodsSpec(mPurchaseBillLine.getGoodsSpec());
            sir.setGoodsUnit(mPurchaseBillLine.getGoodsUnit());
            //备注
            sir.setRemark(mPurchaseBillLine.getRemark());
            //来源采购明细uuid
            sir.setSourceBillLineUuid(mPurchaseBillLine.getUuid());
            //扣重数
            BigDecimal deductQty = getEtBigDecimal(mEtDeductWeight);
            sir.setDeductQty(deductQty);
            // 去皮
            BigDecimal leather = getEtBigDecimal(mEtBucklesLeather);
            sir.setLeatherQty(leather);
            BigDecimal storeIn = getEtBigDecimal(mEtNumWarehousing);
            //判断是重量  数量
            BigDecimal weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
            if (weightCoefficient != null) {
                // 应入库数（当前重量-扣皮）
                sir.setShouldStockInQty(storeIn.add(deductQty.divide(weightCoefficient, RoundingMode.HALF_EVEN)));
                // 实际入库数量（当前重量-扣皮-扣重）
                sir.setQuantity(storeIn);
            } else {
                sir.setShouldStockInQty(storeIn);
                sir.setQuantity(storeIn);
            }
            //标准单位
            sir.setSdUnit(mPurchaseBillLine.getGoodsUnit());
            //类型   	越库的入库类型改为crossDock，入库和越库调拨仍为purchaseIn
            if (mIntTypeUpload == TYPE_CROSS) {
                sir.setStockInType(StockInType.crossDock);
                //生成追溯码，打印分拣标签用
                sir.setPlatformTraceCode(Common.getPlatformTraceCode());
            } else if (mIntTypeUpload == TYPE_STORE_IN) {
                sir.setStockInType(StockInType.purchaseIn);
            } else if (mIntTypeUpload == TYPE_CROSS_ALLOCATE) {
                sir.setStockInType(StockInType.purchaseIn);
                if (mWareHouseTo != null) {
                    sir.setToWarehouse(new UCN(mWareHouseTo.getUuid(), mWareHouseTo.getCode(), mWareHouseTo.getName()));
                }
            }
            //保存顾客，部门信息，打印标签用
            String customer = mPurchaseBillLine.getCustomer() != null ? mPurchaseBillLine.getCustomer().getName() : "";
            String department = mPurchaseBillLine.getCustomerDept() != null ? mPurchaseBillLine.getCustomerDept().getName() : "";
            sir.setCustomerName(customer.concat(department));
            //设置供应商
            sir.setSupplier(mPurchaseData.getSupplier());
            //来源采购单号
            sir.setSourceBillNumber(mPurchaseData.getPurchaseBillNumber());
            //交货日期
            sir.setDeliveryTime(mPurchaseData.getDeliveryTime());
            Date deliveryTime = DateUtils.converToDate(DateUtils.getTime2(mPurchaseData.getDeliveryTime()));
            sir.setDeliveryTime(deliveryTime);
            //入库时间
            sir.setInDate(new Date());
            //设置机构  //操作人名字
            Merchant merchant = LocalSpUtil.getMerchant(this);
            if (merchant != null) {
                //如果没返回org  就是当前登陆的人是公司帐号 就用此帐号的userUuid和name  作为org的IdName
                IdName org = merchant.getOrg();
                if (org == null) {
                    sir.setOrg(new IdName(merchant.getUuid(), merchant.getName()));
                } else {
                    sir.setOrg(org);
                }
            }
            // 原来的扫码获取入库信息改为扫码后，根据扫码内容和上步加载的采购信息匹配即可，
            // 匹配字段为：purchaseBatch，入库、越库、越库调拨操作时需将此码设到StockInRecord的traceCode字段中
            String purchaseBatch = mPurchaseBillLine.getPurchaseBatch();
            if (!TextUtils.isEmpty(purchaseBatch)) {
                sir.setTraceCode(purchaseBatch);
            }
            return sir;
        }
        return null;
    }

    private void clearContent() {
        //清除关键字
        mEtKeySearch.setText("");
        //清除文本
        clearText();
    }

    private void filterList() {
        if (mListAll.size() > 0) {
            List<PurchaseData> listWarehouse = new ArrayList<>();
            Warehouse warehouse = mSpWareHouse.getSelectedItem();
            if (warehouse != null && !TextUtils.isEmpty(warehouse.getName()) && !warehouse.getName().equals("全部")) {
                String warehouseName = warehouse.getName();
                for (PurchaseData data : mListAll) {
                    UCN ucnWarehouse = data.getWarehouse();
                    if (ucnWarehouse != null && ucnWarehouse.getName().equals(warehouseName)) {
                        listWarehouse.add(data);
                    }
                }
            } else {
                listWarehouse.addAll(mListAll);
            }

            String supplierName = mSpSupplier.getSelectedItem();
            List<PurchaseData> listSupplier = new ArrayList<>();
            if (!TextUtils.isEmpty(supplierName) && !supplierName.equals("全部")) {
                for (PurchaseData data : listWarehouse) {
                    IdName supplier = data.getSupplier();
                    if ((supplier != null && supplier.getName().equals(supplierName))) {
                        listSupplier.add(data);
                    }
                }
            } else {
                listSupplier.addAll(listWarehouse);
            }

            String key = mEtKeySearch.getText().toString().trim();
            List<PurchaseData> listKeyName = new ArrayList<>();
            if (!TextUtils.isEmpty(key)) {
                for (PurchaseData data : listSupplier) {
                    UCN goods = data.getPurchaseBillLine().getGoods();
                    if (goods != null && goods.getName().contains(key)) {
                        listKeyName.add(data);
                    }
                }
            } else {
                listKeyName.addAll(listSupplier);
            }
            mListShow.clear();
            mListShow.addAll(listKeyName);
            mAdapterPurchase.notifyDataSetChanged();
        } else {
            mListShow.clear();
            mAdapterPurchase.notifyDataSetChanged();
        }
    }

    @Override
    public void onQueryPurchaseDataSuccess(List<PurchaseData> data) {
        if (data != null && data.size() > 0) {
            List<String> mListSuppliers = new ArrayList<>();
            mListSuppliers.add("全部");
            int size = data.size();
            for (int i = 0; i < size; i++) {
                IdName supplier = data.get(i).getSupplier();
                String supplierName = supplier.getName();
                if (!mListSuppliers.contains(supplierName)) {
                    mListSuppliers.add(supplierName);
                }
            }
            mSpSupplier.setList(mListSuppliers);
            mListAll.clear();
            mListAll.addAll(data);
            filterList();
        } else {
            mListAll.clear();
            mListShow.clear();
            mAdapterPurchase.notifyDataSetChanged();
            mPurchaseData = null;
            mPurchaseBillLine = null;
        }
        dismissLoadingDialog();
    }

    @Override
    public void onQueryPurchaseDataFailed(String message) {
        //清空list跟供应商下拉数据
        mSpSupplier.setList(new ArrayList<String>());
        mListShow.clear();
        mAdapterPurchase.notifyDataSetChanged();
        ToastUtil.showShortToast(getContext(), message);
        dismissLoadingDialog();
    }

    private DBManager getDbManager() {
        if (mDBManager == null) {
            mDBManager = DBManager.getInstance(this.getApplicationContext());
        }
        return mDBManager;
    }

    /**
     * 入库，越库接口请求成功后刷新数据
     *
     * @param record
     */
    private void refreshSuccessData(StockInRecord record) {
        // 保存累计重量
        if (mPurchaseData != null && mPurchaseBillLine != null) {
            BigDecimal accumulate = getEtBigDecimal(mEtWeightAccumulate);
            //转化为kg
            BigDecimal weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
            BigDecimal numWarehousing = getEtBigDecimal(mEtNumWarehousing);
            // 打印标签
            if (mIntTypeUpload == TYPE_STORE_IN || mIntTypeUpload == TYPE_CROSS_ALLOCATE) {
                String goodsName = mPurchaseBillLine.getGoods().getName();
                String purchaseBatch = mPurchaseBillLine.getPurchaseBatch();
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                PrinterInventory.printer(getContext(), count, goodsName, purchaseBatch);
            } else if (mIntTypeUpload == TYPE_CROSS) {
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                String goodsName = mPurchaseBillLine.getGoods().getName();
                String crossNum;
                if (weightCoefficient != null && weightCoefficient.doubleValue() != 0) {
                    crossNum = BigDecimalUtil.toScaleStr(numWarehousing.multiply(weightCoefficient)).concat("kg");
                } else {
                    crossNum = BigDecimalUtil.toScaleStr(numWarehousing).concat(mPurchaseBillLine.getGoodsUnit().getName());
                }
                String code = record.getPlatformTraceCode();
                String customer = mPurchaseBillLine.getCustomer() != null ? mPurchaseBillLine.getCustomer().getName() : "";
                String department = mPurchaseBillLine.getCustomerDept() != null ? mPurchaseBillLine.getCustomerDept().getName() : "";
                PrinterSortOut.printer(
                        getContext(),
                        count,
                        PrinterSortOut.SORT_OUT_QRCODE.concat(code),
                        customer,
                        department,
                        goodsName,
                        crossNum,
                        code);
            }
            //设置累计
            if (weightCoefficient != null) {
                mMapAccumulate.put(mPurchaseBillLine.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing.multiply(weightCoefficient))));
            } else {
                mMapAccumulate.put(mPurchaseBillLine.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing)));
            }
            //保存已入库数
            mPurchaseBillLine.setHasStockInQty(mPurchaseBillLine.getHasStockInQty().add(getEtBigDecimal(mEtNumWarehousing)));
            clearContent();
            mAdapterPurchase.notifyDataSetChanged();
            mPurchaseData = null;
            mPurchaseBillLine = null;
            mWareHouseTo = null;
            //光标移动到采购标签
            mEtPurChaseLabel.requestFocus();
        }
    }

    /**
     * 采购订单Adapter
     **/
    private class PurchaseBillLineAdapter extends CommonAdapter4Lv<PurchaseData> {

        private PurchaseBillLineAdapter(Context context, List<PurchaseData> data) {
            super(context, R.layout.item_line, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, PurchaseData data, int position) {
            PurchaseBillLine line = data.getPurchaseBillLine();
            //商品名称
            String name = line.getGoods().getName();
            //采购数
            BigDecimal purchaseQty = line.getPurchaseQty();
            //入库数
            BigDecimal hasStockInQty = line.getHasStockInQty();
            //单位
            String unit = line.getGoodsUnit().getName();
            holder.setText(R.id.tv_goods_name, name)
                    .setText(R.id.tv_goods_purchase, "采购数:" + BigDecimalUtil.toScaleStr(purchaseQty) + unit)
                    .setText(R.id.tv_goods_sort_out, "入库数:" + BigDecimalUtil.toScaleStr(hasStockInQty) + unit);

        }
    }

    /**
     * 称 重量接收器
     */
    String mPreWeight;

    @Override
    public void receive(Instrument.InsData data) {
        if (mEtWeightCurrent == null) {
            return;
        }
        if (data == null) {
            setEtWeightCurrent("");
        } else {
            if (isWeight()) {
                //重量商品才进行称重
                mEtWeightCurrent.setText(data.weight);
                if (data.stable && !TextUtils.equals(data.weight, mPreWeight)) {
                    //设置设置入库数量
                    BigDecimal currentWeight = getEtBigDecimal(mEtWeightCurrent);
                    BigDecimal leather = getEtBigDecimal(mEtBucklesLeather);
                    BigDecimal deduct = getEtBigDecimal(mEtDeductWeight);
                    BigDecimal stockInNum = currentWeight.subtract(leather).subtract(deduct);
                    //转化单位
                    BigDecimal defaultWeightCoefficient = new BigDecimal(1);
                    if (mPurchaseBillLine.getWeightCoefficient().doubleValue() != 0) {
                        defaultWeightCoefficient = mPurchaseBillLine.getWeightCoefficient();
                    }
                    BigDecimal finallyCount = stockInNum.divide(defaultWeightCoefficient, RoundingMode.HALF_EVEN);
                    if (finallyCount.doubleValue() < 0) {
                        mEtNumWarehousing.setText("0.00");
                    } else {
                        mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(finallyCount));
                    }
                }
                mPreWeight = data.weight;
            }
        }
    }

    private void setEtWeightCurrent(String weight) {
        mEtWeightCurrent.setText(weight);
        mPreWeight = weight;
    }

    private BigDecimal getEtBigDecimal(EditText et) {
        String str = et.getText().toString().trim();
        return new BigDecimal(!TextUtils.isEmpty(str) ? str : "0");
    }

    private void setText(TextView tv, String content) {
        if (!TextUtils.isEmpty(content)) {
            tv.setText(content);
        } else {
            tv.setText("");
        }
    }

    private void setEditText(EditText et, String content) {
        if (!TextUtils.isEmpty(content)) {
            et.setText(content);
        } else {
            et.setText("");
        }
    }

    private View getEditText(int viewGroupId, String title) {
        View viewGroup = findViewById(viewGroupId);
        ((TextView) viewGroup.findViewById(R.id.tv_title)).setText(title);
        return viewGroup.findViewById(R.id.ed_content);
    }

}
