package xm.cloudweight;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.process.ForecastProcessPlan;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.fragment.InputFragment;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.KeyBoardUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.MessageUtil;
import xm.cloudweight.utils.dao.bean.DbRequestData;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static xm.cloudweight.service.RequestDataService.TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA;
import static xm.cloudweight.service.RequestDataService.TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA_FAILED;

/**
 * @author : wyh
 * @create : 2018/2/26
 * @des :  加工入库
 */
public class ProcessStorageActivity extends BaseActivity implements VideoFragment.OnInstrumentListener, AdapterView.OnItemSelectedListener {

    private static final String DEFAULT_SPINNER_CUSTOMER = "客户";
    private VideoFragment mVideoFragment;
    private InputFragment mInputFragment;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.et_finish_produce_label)
    ScanEditText mEtFinishProduceLabel;
    @BindView(R.id.iv_delete)
    ImageView mIvDelete;
    @BindView(R.id.et_key_search)
    EditText mEtKeySearch;
    @BindView(R.id.sp_customer)
    DataSpinner<String> mSpCustomer;
    @BindView(R.id.sp_ware_house_in)
    DataSpinner<Warehouse> mSpWareHouseIn;
    @BindView(R.id.lv_line)
    ListView mLvData;
    @BindView(R.id.tv_info_goods)
    TextView mTvInfoGood;
    @BindView(R.id.tv_info_specification)
    TextView mTvInfoSpecification;
    @BindView(R.id.tv_info_customer_remark)
    TextView mTvInfoCustomerRemark;
    private List<ForecastProcessPlan> mListShow = new ArrayList<>();
    private List<ForecastProcessPlan> mListAll = new ArrayList<>();
    private EditText mEtProduceAccumulate;
    private EditText mEtWeightCurrent;
    private SearchAndFocusEditText mEtBucklesLeather;
    private SearchAndFocusEditText mEtDeductBad;
    private SearchAndFocusEditText mEtDeduceProduction;
    private SearchAndFocusEditText mEtStorageInPrice;
    private ProcessAdapter mProcessAdapter;
    private TextView mTvDeductBadUnit;
    private TextView mTvDeductProductionUnit;

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
        mTvDeductBadUnit = findViewById(R.id.ll_deduct_bad).findViewById(R.id.tv_unit);
        mEtDeduceProduction = (SearchAndFocusEditText) getEditText(R.id.ll_item_production, "产量");
        mTvDeductProductionUnit = findViewById(R.id.ll_item_production).findViewById(R.id.tv_unit);
        mEtStorageInPrice = (SearchAndFocusEditText) getEditText(R.id.ll_storage_in_price, "入库单价");
        findViewById(R.id.ll_storage_in_price).findViewById(R.id.tv_unit).setVisibility(View.INVISIBLE);

        mProcessAdapter = new ProcessAdapter(getContext(), mListShow);
        mLvData.setAdapter(mProcessAdapter);
        mLvData.setOnItemClickListener(mOnItemClickListener);

        //设置监听
        mEtFinishProduceLabel.setOnScanFinishListener(mLabelOnScanFinishListener);
        mEtBucklesLeather.setOnInputFinishListener(mOnInputFinishListener);
        mEtDeductBad.setOnInputFinishListener(mOnInputFinishListener);
        mSpCustomer.setCustomItemSelectedListener(this);
        mSpWareHouseIn.setCustomItemSelectedListener(this);
        mSpWareHouseIn.setTitleColor(R.color.color_135c31);

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
        //获取仓库
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(getContext());
        if (listWareHouse != null && listWareHouse.size() > 0) {
            Warehouse wareHouseEmpty = new Warehouse();
            wareHouseEmpty.setName(" ");
            listWareHouse.add(0, wareHouseEmpty);
            mSpWareHouseIn.setList(listWareHouse);
        } else {
            ToastUtil.showShortToast(getContext(), "无入库仓库");
        }
        //查询加工数据
        showLoadingDialog(true);
        queryProcessData(currentData);
    }

    private Handler mRequestData = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long type = MessageUtil.getType(msg);
            if (type == TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA) {
                queryProcessDataSuccess(type);
            } else if (type == TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA_FAILED) {
                queryProcessDataFailed(msg);
            }
            return false;
        }
    });

    /**
     * 查询加工数据
     */
    private void queryProcessData(String date) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("createdTime", date);
            getIRequestDataService().onGetDataListener(TYPE_PROCESS_STORAGE_QUERY_PROCESS_DATA, params, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    Message message = MessageUtil.create(type, null);
                    mRequestData.sendMessage(message);
                }

                @Override
                public void onError(long type, String message) throws RemoteException {
                    Message msg = MessageUtil.create(type, message);
                    mRequestData.sendMessage(msg);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找加工数据成功
     */
    private void queryProcessDataSuccess(long type) {
        List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
        String data = dbRequestData.get(0).getData();
        List<ForecastProcessPlan> result = GsonUtil.getGson().fromJson(data, new TypeToken<List<ForecastProcessPlan>>() {
        }.getType());
        if (result != null && result.size() > 0) {
            //遍历出客户列表
            List<String> listCustomer = new ArrayList<>();
            listCustomer.add(DEFAULT_SPINNER_CUSTOMER);
            int size = result.size();
            for (int i = 0; i < size; i++) {
                String customerName = result.get(i).getCustomerName();
                if (TextUtils.isEmpty(customerName)) {
                    continue;
                }
                if (!listCustomer.contains(customerName)) {
                    listCustomer.add(customerName);
                }
            }
            mSpCustomer.setList(listCustomer);
            mListAll.clear();
            mListAll.addAll(result);
            filterList();
        } else {
            mListShow.clear();
            mListAll.clear();
            mProcessAdapter.notifyDataSetChanged();
            mForecastProcessPlan = null;
        }
        dismissLoadingDialog();
    }

    /**
     * 查找加工数据失败
     */
    private void queryProcessDataFailed(Message msg) {
        String failedMsg = MessageUtil.getObj(msg);
        ToastUtil.showShortToast(getContext(), failedMsg);
        dismissLoadingDialog();
    }

    private void filterList() {
        if (mListAll.size() > 0) {
            //过滤客户
            List<ForecastProcessPlan> lisWareHouse = new ArrayList<>();
            String customerName = mSpCustomer.getSelectedItem();
            if (!customerName.equals(DEFAULT_SPINNER_CUSTOMER)) {
                for (ForecastProcessPlan plan : mListAll) {
                    if (plan.getCustomerName().equals(customerName)) {
                        lisWareHouse.add(plan);
                    }
                }
            } else {
                lisWareHouse.addAll(mListAll);
            }
            //过滤关键字
            String key = mEtKeySearch.getText().toString().trim();
            List<ForecastProcessPlan> listKeyName = new ArrayList<>();
            if (!TextUtils.isEmpty(key)) {
                for (ForecastProcessPlan data : lisWareHouse) {
                    UCN goods = data.getGoods();
                    if (goods != null && goods.getName().contains(key)) {
                        listKeyName.add(data);
                    }
                }
            } else {
                listKeyName.addAll(lisWareHouse);
            }
            mListShow.clear();
            mListShow.addAll(listKeyName);
            mProcessAdapter.notifyDataSetChanged();
        } else {
            mListShow.clear();
            mProcessAdapter.notifyDataSetChanged();
        }
    }

    private ForecastProcessPlan mForecastProcessPlan;
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mForecastProcessPlan = mListShow.get(position);
            setForecastProcessPlan();
        }
    };

    /**
     * 设置信息
     */
    private void setForecastProcessPlan() {
        clearText();
        BigDecimal weightCoefficient = mForecastProcessPlan.getWeightCoefficient();
        if (weightCoefficient != null) {
            mTvDeductProductionUnit.setText("kg");
            mTvDeductBadUnit.setText("kg");
            mEtBucklesLeather.setEnabled(true);
        } else {
            String name = mForecastProcessPlan.getGoodsUnit().getName();
            mTvDeductProductionUnit.setText(name);
            mTvDeductBadUnit.setText(name);
            mEtBucklesLeather.setEnabled(false);
        }
        //商品名
        UCN goods = mForecastProcessPlan.getGoods();
        setText(mTvInfoGood, (goods != null && !TextUtils.isEmpty(goods.getName())) ? goods.getName() : "");
        //规格
        String packageSpec = mForecastProcessPlan.getPackageSpec();
        setText(mTvInfoSpecification, !TextUtils.isEmpty(packageSpec) ? packageSpec : "");
        //客户
        String customerName = mForecastProcessPlan.getCustomerName();
        setText(mTvInfoCustomerRemark, !TextUtils.isEmpty(customerName) ? "客户：" + customerName : "客户：");
        //累计产量
        BigDecimal completeQty = mForecastProcessPlan.getCompleteQty();
        mEtProduceAccumulate.setText(BigDecimalUtil.toScaleStr(completeQty));
        //关闭数字键盘
        if (mInputFragment != null) {
            mInputFragment.hide();
        }
    }

    /**
     * 清除商品信息
     */
    private void clearText() {
        mTvInfoGood.setText("");
        mTvInfoSpecification.setText("");
        mTvInfoCustomerRemark.setText("客户：");
        mEtProduceAccumulate.setText("");
        setEtWeightCurrent("");
        mEtBucklesLeather.setText("");
        mEtDeductBad.setText("");
        mTvDeductBadUnit.setText("");
        mEtDeduceProduction.setText("");
        mTvDeductProductionUnit.setText("");
        mEtStorageInPrice.setText("");
    }

    private void setEtWeightCurrent(String weight) {
        mEtWeightCurrent.setText(weight);
        mPreWeight = weight;
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
            et.setSelection(content.length());
        } else {
            et.setText("");
        }
    }

    /**
     * 关键字搜索
     */
    private void keySearch() {
        showLoadingDialog(false);
        filterList();
        dismissLoadingDialog();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (view.getId()) {
            case R.id.sp_customer:
                showLoadingDialog(false);
                filterList();
                dismissLoadingDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @OnClick({R.id.btn_date, R.id.btn_clear_zero, R.id.et_key_search})
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
            case R.id.et_key_search:
                keySearch();
                break;
            default:
                break;
        }
    }

    private onInputFinishListener mOnInputFinishListener = new onInputFinishListener() {
        @Override
        public void onFinish(String key) {
            if (mForecastProcessPlan == null) {
                return;
            }
            String strCurrentWeight = mEtWeightCurrent.getText().toString().trim();
            BigDecimal currentWeight = new BigDecimal(!TextUtils.isEmpty(strCurrentWeight) ? strCurrentWeight : "0");
            String strLeather = mEtBucklesLeather.getText().toString().trim();
            BigDecimal leather = new BigDecimal(!TextUtils.isEmpty(strLeather) ? strLeather : "0");
            String strDeductBad = mEtDeductBad.getText().toString().trim();
            BigDecimal deductBad = new BigDecimal(!TextUtils.isEmpty(strDeductBad) ? strDeductBad : "0");
            BigDecimal weightCoefficient = mForecastProcessPlan.getWeightCoefficient();
            BigDecimal finallyCount;
            if (weightCoefficient != null) {
                finallyCount = currentWeight.subtract(leather).subtract(deductBad).divide(weightCoefficient, ROUND_HALF_EVEN);
            } else {
                finallyCount = currentWeight;
            }
            if (finallyCount.doubleValue() < 0) {
                mEtDeduceProduction.setText("0.00");
            } else {
                String num = BigDecimalUtil.toScaleStr(finallyCount);
                mEtDeduceProduction.setText(num);
            }
        }
    };

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
        if (mListAll.size() == 0) {
            ToastUtil.showShortToast(getContext(), "暂无数据");
            return;
        }
        for (ForecastProcessPlan data : mListAll) {
            String traceCode = data.getTraceCode();
            if (purchaseBatchScan.equals(traceCode)) {
                mForecastProcessPlan = data;
                setForecastProcessPlan();
                mEtFinishProduceLabel.setText("");
                dismissLoadingDialog();
                break;
            }
        }
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
            queryProcessData(date);
        }
    };

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
                    //设置产量
                    BigDecimal currentWeight = getEtBigDecimal(mEtWeightCurrent);
                    BigDecimal leather = getEtBigDecimal(mEtBucklesLeather);
                    BigDecimal bad = getEtBigDecimal(mEtDeductBad);
                    BigDecimal stockInNum = currentWeight.subtract(leather).subtract(bad);
                    //转化单位
                    BigDecimal defaultWeightCoefficient = new BigDecimal(1);
                    if (mForecastProcessPlan.getWeightCoefficient().doubleValue() != 0) {
                        defaultWeightCoefficient = mForecastProcessPlan.getWeightCoefficient();
                    }
                    BigDecimal finallyCount = stockInNum.divide(defaultWeightCoefficient, RoundingMode.HALF_EVEN);
                    if (finallyCount.doubleValue() < 0) {
                        mEtDeduceProduction.setText("0.00");
                    } else {
                        mEtDeduceProduction.setText(BigDecimalUtil.toScaleStr(finallyCount));
                    }
                }
                mPreWeight = data.weight;
            }
        }
    }

    /**
     * 当前选择为数量单位
     */
    private boolean isCount() {
        return mForecastProcessPlan != null && mForecastProcessPlan.getWeightCoefficient() == null;
    }

    /**
     * 当前选择为重量单位
     */
    private boolean isWeight() {
        return mForecastProcessPlan != null
                && mForecastProcessPlan.getWeightCoefficient() != null
                && mForecastProcessPlan.getWeightCoefficient().doubleValue() != 0;
    }

    private BigDecimal getEtBigDecimal(EditText et) {
        String str = et.getText().toString().trim();
        return new BigDecimal(!TextUtils.isEmpty(str) ? str : "0");
    }

    private View getEditText(int viewGroupId, String title) {
        View viewGroup = findViewById(viewGroupId);
        ((TextView) viewGroup.findViewById(R.id.tv_title)).setText(title);
        return viewGroup.findViewById(R.id.ed_content);
    }

    private class ProcessAdapter extends CommonAdapter4Lv<ForecastProcessPlan> {

        private ProcessAdapter(Context context, List<ForecastProcessPlan> data) {
            super(context, R.layout.item_process_storge, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, ForecastProcessPlan plan, int position) {
            //商品名称
            String name = plan.getGoods().getName();
            // 计划产量
            BigDecimal planQty = plan.getPlanQty();
            // 累计产量
            BigDecimal completeQty = plan.getCompleteQty();
            //单位
            String unit = plan.getGoodsUnit().getName();
            //客户
            String customerName = plan.getCustomerName();
            holder.setText(R.id.tv_goods_name, name)
                    .setText(R.id.tv_goods_plan, "计划产量:" + BigDecimalUtil.toScaleStr(planQty) + unit)
                    .setText(R.id.tv_goods_cumulative, "累计产量:" + BigDecimalUtil.toScaleStr(completeQty) + unit)
                    .setText(R.id.tv_goods_customer, !TextUtils.isEmpty(customerName) ? customerName : "");
        }
    }

}
