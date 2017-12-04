package xm.cloudweight;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.sort.SortOutData;
import com.xmzynt.storm.service.user.customer.Customer;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.user.customer.MerchantCustomer;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.PageData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.BeanPrinter;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.impl.CommImpl;
import xm.cloudweight.impl.SortOutImpl;
import xm.cloudweight.presenter.CommPresenter;
import xm.cloudweight.presenter.SortOutPresenter;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.EtMaxLengthUtil;
import xm.cloudweight.utils.bussiness.FilterUtil;
import xm.cloudweight.utils.bussiness.ListComparator;
import xm.cloudweight.utils.bussiness.PrinterUtil;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.PrinterView;
import xm.cloudweight.widget.ScalableTextView;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.SortOutHistoryPopWindow;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

/**
 * @author wyh
 * @Description: 分拣
 * @create 2017/10/29
 */
public class SortOutActivity extends BaseActivity implements
        CommImpl.OnGetWareHousesListener
        , CommImpl.OnQueryStockListener
        , SortOutImpl.OnGetMerchantCustomerListener
        , SortOutImpl.OnGetDropDownLevelsListener
        , SortOutImpl.OnGetSortOutListListener
        , SortOutImpl.OnCancelSortOutListener
        , AdapterView.OnItemSelectedListener
        , SortOutHistoryPopWindow.OnDeleteListener, onInputFinishListener, VideoFragment.OnInstrumentListener {

    public static final int TYPE_WEIGHT = 0;
    public static final int TYPE_COUNT = 1;

    @BindView(R.id.btn_sort_out_history)
    Button mBtnSortOutHistory;
    @BindView(R.id.sp_ware_house)
    DataSpinner<Warehouse> mSpWareHouse;
    @BindView(R.id.sp_customers)
    DataSpinner<MerchantCustomer> mSpCustomers;
    @BindView(R.id.sp_customers_level)
    DataSpinner<CustomerLevel> mSpCustomersLevel;
    @BindView(R.id.gv_sort_out)
    GridView mGvSortOut;
    @BindView(R.id.btn_sort_out_weight)
    Button mBtnSortOutWeight;
    @BindView(R.id.btn_sort_out_count)
    Button mBtnSortOutCount;
    @BindView(R.id.tv_type)
    TextView mTvType;
    @BindView(R.id.tv_type_unit)
    TextView mTvTypeUnit;
    @BindView(R.id.et_show)
    EditText mEtShow;
    @BindView(R.id.et_basket)
    ScanEditText mEtBasket;
    @BindView(R.id.et_goods_name_or_id)
    SearchAndFocusEditText mEtGoodsNameOrId;
    @BindView(R.id.et_custom_group)
    SearchAndFocusEditText mEtCustomGroup;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.tv_amount)
    TextView mTvAmount;
    @BindView(R.id.btn_sort_out)
    Button mBtnSortOut;
    private List<SortOutData> mListShow = new ArrayList<>();
    private List<SortOutData> mListAll = new ArrayList<>();
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private SortOutAdapter mSortOutAdapter;
    private int mIntType = TYPE_WEIGHT;
    private boolean loadCustomer;
    private boolean loadCustomerLevel;
    private boolean loadSortOutList;
    private SortOutData mPreSortOutData;
    private SortOutHistoryPopWindow mHistoryPopWindow;
    private DBManager mDBManager;
    private DbImageUpload mDbImageUpload;
    private PrinterView mPrinterView;
    private boolean mStable;
    private VideoFragment mVideoFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sort_out;
    }

    @Override
    protected void initContentView() {
        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mVideoFragment).commitAllowingStateLoss();
        //判断有没网络
        changeNetState(inspectNet());
        mEtShow.setEnabled(false);
        mSortOutAdapter = new SortOutAdapter(getContext(), mListShow);
        mGvSortOut.setAdapter(mSortOutAdapter);
        mGvSortOut.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPreSortOutData = mListShow.get(i);
                mSortOutAdapter.setIntSelect(i);
                mSortOutAdapter.notifyDataSetChanged();
                notifyItemClick();
            }
        });
        mSpCustomers.setTitleColor(R.color.color_135c31);
        mSpWareHouse.setTitleColor(R.color.color_135c31);
        mSpCustomersLevel.setTitleColor(R.color.color_135c31);
        mSpCustomers.setCustomItemSelectedListener(this);
        mSpCustomersLevel.setCustomItemSelectedListener(this);
        mSpWareHouse.setCustomItemSelectedListener(this);
        mEtCustomGroup.setOnInputFinishListener(this);
        mEtGoodsNameOrId.setOnInputFinishListener(this);
        mEtBasket.setFilters(EtMaxLengthUtil.getFilter());
        mEtBasket.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key)) {
                    if (key.length() == Common.BasketLength) {
                        CommPresenter.queryStock(getActivity(), 0, 0, 0, key);
                    }
                } else {
                    SortOutPresenter.getSourOutList(getActivity(), TYPE_WEIGHT, 0, 0, 0, mBtnDate.getText().toString());
                }
            }
        });
    }

    private void notifyItemClick() {
        if (mIntType == TYPE_COUNT) {
            //显示数量
            BigDecimal hasStockOutQty = mPreSortOutData.getHasStockOutQty() != null ? mPreSortOutData.getHasStockOutQty() : new BigDecimal(0);
            String scaleStr = BigDecimalUtil.toScaleStr(mPreSortOutData.getGoodsQty().subtract(hasStockOutQty));
            mEtShow.setText(scaleStr);
            mEtShow.setSelection(scaleStr.length());
            String unit = mPreSortOutData.getGoodsUnit().getName();
            mTvTypeUnit.setText(unit);
        } else {
            mEtShow.setText("");
            mTvTypeUnit.setText("kg");
        }
    }

    @Override
    protected void loadDate() {
        CommPresenter.getListDate(this);
        CommPresenter.getListWareHouse(this);
        SortOutPresenter.getDropdownCustomers(this, 0, 0, 0);
        SortOutPresenter.getDropDownLevels(this);

        mBtnSortOutCount.setSelected(false);
        mBtnSortOutWeight.setSelected(true);
        //设置当前日期
        String currentData = DateUtils.StringData();
        mBtnDate.setText(currentData);
        SortOutPresenter.getSourOutList(this, TYPE_WEIGHT, 0, 0, 0, currentData);
    }

    @Override
    public void onNetChange(int netMobile) {
        super.onNetChange(netMobile);
        boolean netConnect = isNetConnect();
        changeNetState(netConnect);
    }

    private void changeNetState(boolean netConnect) {
        if (netConnect) {
            mEtBasket.setEnabled(true);
            mEtBasket.requestFocus();
        } else {
            mEtBasket.setEnabled(false);
            mEtGoodsNameOrId.requestFocus();
        }
    }

    @OnClick({R.id.btn_sort_out_weight, R.id.btn_sort_out_count, R.id.btn_sort_out_history, R.id.btn_sort_out, R.id.btn_date})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_date:
                //显示时间选择框
                String dateStr = mBtnDate.getText().toString().trim();
                DatePickerDialogUtil.show(getContext(), dateStr, mOnDateSetListener);
                break;
            case R.id.btn_sort_out_weight: {
                if (mIntType == TYPE_WEIGHT) {
                    return;
                }
                mBtnSortOutWeight.setSelected(true);
                mBtnSortOutCount.setSelected(false);
                mTvType.setText("称重数：");
                mEtShow.setText("");
                mEtShow.setEnabled(false);
                mIntType = TYPE_WEIGHT;
                String time = mBtnDate.getText().toString().trim();
                SortOutPresenter.getSourOutList(this, TYPE_WEIGHT, 0, 0, 0, time);
                break;
            }
            case R.id.btn_sort_out_count: {
                if (mIntType == TYPE_COUNT) {
                    return;
                }
                mBtnSortOutCount.setSelected(true);
                mBtnSortOutWeight.setSelected(false);
                mTvType.setText("出库数量：");
                mEtShow.setText("");
                mEtShow.setEnabled(true);
                mIntType = TYPE_COUNT;
                String time = mBtnDate.getText().toString().trim();
                SortOutPresenter.getSourOutList(this, TYPE_COUNT, 0, 0, 0, time);
                break;
            }
            case R.id.btn_sort_out_history:
                toHistory();
                break;
            case R.id.btn_sort_out:
                if (mPreSortOutData == null) {
                    ToastUtil.showShortToast(getContext(), "请选择商品");
                    return;
                }
                String countStr = mEtShow.getText().toString().trim();
                if (TextUtils.isEmpty(countStr)) {
                    ToastUtil.showShortToast(getContext(), "请输入重量/数量");
                    return;
                }
                Warehouse selectedWareHouse = mSpWareHouse.getSelectedItem();
                if (selectedWareHouse == null) {
                    ToastUtil.showShortToast(getContext(), "请选择仓库");
                    return;
                }
                mBtnSortOut.setEnabled(false);
                if (mVideoFragment.isLight()) {
                    mVideoFragment.screenshot(mHandlerShotPic);
                } else {
                    showP();
                    setDataToDb(null);
                    setBtnEnable();
                    dismissP();
                }
                break;
            default:
                break;
        }
    }

    private Handler mHandlerShotPic = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showP();
                    setBtnEnable();
                    break;
                case 2:
                    dismissP();
                    //保存图片路径都后台作为请求
                    String path = msg.getData().getString("path", "");
                    ToastUtil.showShortToast(getContext(), "已添加到分拣上传队列");
                    setDataToDb(path);
                    setBtnEnable();
                    break;
                case 3:
                    dismissP();
                    setDataToDb(null);
                    setBtnEnable();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    /**
     * 设置按钮是否可用
     */
    private void setBtnEnable() {
        if (!mBtnSortOut.isEnabled()) {
            mBtnSortOut.setEnabled(true);
        }
    }

    /**
     * 保存数据到数据库   更新列表
     */
    private void setDataToDb(String path) {
        if (mPreSortOutData != null) {
            // 保存 称重数（订购数量）   直接上传kg数   不需要转换
            String countStr = mEtShow.getText().toString().trim();
            if (mPreSortOutData.getUnitCoefficient() != null && mPreSortOutData.getUnitCoefficient().doubleValue() != 0) {
                mPreSortOutData.setStockOutQty(new BigDecimal(countStr).divide(mPreSortOutData.getUnitCoefficient(), RoundingMode.HALF_EVEN));
            } else {
                mPreSortOutData.setStockOutQty(new BigDecimal(countStr));
            }
            // 保存 仓库
            Warehouse warehouse = mSpWareHouse.getSelectedItem();
            mPreSortOutData.setWarehouse(new UCN(warehouse.getUuid(), warehouse.getCode(), warehouse.getName()));
            // ** 保存数据到数据库在 setStockOutQty，setWarehouse 后
            DbImageUpload dbImageUpload = new DbImageUpload();
            dbImageUpload.setDate(mBtnDate.getText().toString().trim());
            dbImageUpload.setLine(GsonUtil.getGson().toJson(mPreSortOutData));
            dbImageUpload.setImagePath(path);
            dbImageUpload.setType(Common.DbType.TYPE_SORT_OUT_STORE_OUT);
            getDbManager().insertDbImageUpload(dbImageUpload);

            //更新数据
            //****已经分拣过的数据，且累计分拣数量>90%采购数量的，状态改成“已分拣”，界面不再显示****
            //采购数(数量.重量)
            BigDecimal goodsQty = mPreSortOutData.getGoodsQty();
            //出库数
            BigDecimal stockOutQty = mPreSortOutData.getStockOutQty();
            //已出出库数
            BigDecimal hasStockOutQty = mPreSortOutData.getHasStockOutQty();
            //总出库数   hasStockOutQty总是有值，不用判空
            BigDecimal unitCoefficient = mPreSortOutData.getUnitCoefficient();
            BigDecimal outAmount = hasStockOutQty.add(stockOutQty);
            BigDecimal qtyOfNinety = goodsQty.multiply(new BigDecimal(0.9));
            if (outAmount.compareTo(qtyOfNinety) > 0) {
                //界面不显示
                //从列表中删除
                if (mListShow.contains(mPreSortOutData)) {
                    mListShow.remove(mPreSortOutData);
                }
            } else {
                //显示已出 数量
                if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
                    mPreSortOutData.setCoverToKgQty(mPreSortOutData.getCoverToKgQty().subtract(stockOutQty.multiply(unitCoefficient)));
                }
                mPreSortOutData.setHasStockOutQty(outAmount);
            }

            // 打印标签
            if (mPrinterView == null) {
                mPrinterView = (PrinterView) findViewById(R.id.pv);
            }

            //打印标签
            printer(unitCoefficient);

            //先打印后清除数据
            mPreSortOutData.setStockOutQty(null);
            mPreSortOutData.setWarehouse(null);

            //默认设置第一个
            mSortOutAdapter.setIntSelect(0);
            mSortOutAdapter.notifyDataSetChanged();
            if (mListShow.size() > 0) {
                mPreSortOutData = mListShow.get(0);
                notifyItemClick();
            }

        }
    }

    private void printer(BigDecimal unitCoefficient) {
        BeanPrinter beanPrinter = new BeanPrinter();
        beanPrinter.setGoodsName(mPreSortOutData.getGoods().getName());
        if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
            beanPrinter.setCount(BigDecimalUtil.toScaleStr(mPreSortOutData.getStockOutQty().multiply(unitCoefficient)).concat("kg"));
        } else {
            beanPrinter.setCount(BigDecimalUtil.toScaleStr(mPreSortOutData.getStockOutQty()).concat(mPreSortOutData.getGoodsUnit().getName()));
        }
        IdName customer = mPreSortOutData.getCustomer();
        beanPrinter.setCustomer(customer != null && !TextUtils.isEmpty(customer.getName()) ? customer.getName() : null);
        IdName customerDepartment = mPreSortOutData.getCustomerDepartment();
        beanPrinter.setDepartment(customerDepartment != null && !TextUtils.isEmpty(customerDepartment.getName()) ? customerDepartment.getName() : null);
        //设置数据
        mPrinterView.set(beanPrinter);
        String filePath = mPrinterView.getPath();
        PrinterUtil.printer(this, filePath);
    }

    /**
     * 获取数据库管理器
     */
    private DBManager getDbManager() {
        if (mDBManager == null) {
            mDBManager = DBManager.getInstance(this.getApplicationContext());
        }
        return mDBManager;
    }

    /**
     * 时间选择监听
     */
    private DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            String monthStr = ((month + 1) < 10) ? "0" + (month + 1) : (month + 1) + "";
            String dayStr = (dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth + "";
            String date = year + "-" + monthStr + "-" + dayStr;
            mBtnDate.setText(date);
            //请求数据
            SortOutPresenter.getSourOutList(getActivity(), mIntType, 0, 0, 0, date);
        }
    };

    /**
     * 弹窗
     */
    private void toHistory() {
        if (mHistoryPopWindow == null) {
            mHistoryPopWindow = new SortOutHistoryPopWindow(this, mBtnSortOutHistory);
            mHistoryPopWindow.setOnDeleteListener(this);
        }
        refreshHistoryList();

    }

    /**
     * 刷新历史列表
     */
    private void refreshHistoryList() {
        //获取数据库当天的数据
        String date = mBtnDate.getText().toString().trim();
        List<DbImageUpload> daoList = getDbManager().getDbListSortOutStoreOutHistory(date);
        Collections.reverse(daoList);
        mListHistory.clear();
        mListHistory.addAll(daoList);
        mHistoryPopWindow.show();
        mHistoryPopWindow.notify(mListHistory);
    }

    @Override
    public void delete(DbImageUpload dbImageUpload) {
        if (!TextUtils.isEmpty(dbImageUpload.getStockOutUuid())) {
            //已分拣 请求接口撤销
            mDbImageUpload = dbImageUpload;
            SortOutData data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), SortOutData.class);
            SortOutPresenter.cancelSortOut(getActivity(), data);
        } else {
            //未分拣 删除本地数据库
            getDbManager().deleteDbImageUpload(dbImageUpload);
            refreshHistoryList();
            ToastUtil.showShortToast(getContext(), "撤销分拣成功");
        }
    }

    @Override
    public void getMerchantCustomerSuccess(List<MerchantCustomer> list) {
        MerchantCustomer e = new MerchantCustomer();
        Customer customer = new Customer();
        customer.setName("全部");
        e.setCustomer(customer);
        list.add(0, e);
        mSpCustomers.setList(list);
        loadCustomer = true;
        filterList();
    }

    @Override
    public void getMerchantCustomerFailed(int errorType, String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void getWareHousesSuccess(List<Warehouse> list) {
        mSpWareHouse.setList(list);
    }

    @Override
    public void getWareHousesFailed(int errorType, String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void getDropDownLevelsSuccess(List<CustomerLevel> list) {
        CustomerLevel e = new CustomerLevel();
        e.setName("全部");
        list.add(0, e);
        mSpCustomersLevel.setList(list);
        loadCustomerLevel = true;
        filterList();
    }

    @Override
    public void getDropDownLevelsFailed(int errorType, String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void getSortOutListSuccess(int type, List<SortOutData> data) {
        mListAll.clear();
        mListAll.addAll(data);
        loadSortOutList = true;
        //筛选条件
        filterList();
    }

    @Override
    public void getSortOutListFailed(int type, String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void onCancelSortOutSuccess(SortOutData data) {
        getDbManager().deleteDbImageUpload(mDbImageUpload);
        mDbImageUpload = null;
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销分拣成功");
    }

    @Override
    public void onCancelSortOutFailed(String message) {
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_customers:
            case R.id.sp_customers_level:
                filterList();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * 过滤列表
     */
    private void filterList() {
        //等待全部加载后再筛选
        if (!loadSortOutList || !loadCustomer || !loadCustomerLevel) {
            return;
        }
        mTvAmount.setText("汇总：");
        if (mListAll == null
                || mListAll.size() == 0) {
            mSortOutAdapter.setIntSelect(0);
            mListShow.clear();
            mSortOutAdapter.notifyDataSetChanged();
            return;
        }

        //逐级删除不符合的数据
//        ArrayList<SortOutData> filterList = FilterUtil.filter(mListAll, mSpCustomersLevel, mSpCustomers, mEtGoodsNameOrId, mEtBasket, mEtCustomGroup);
        ArrayList<SortOutData> filterList = FilterUtil.filter(mListAll, mSpCustomersLevel, mSpCustomers, mEtGoodsNameOrId, mEtCustomGroup);

        mListShow.clear();
        mListShow.addAll(filterList);
        if (mListShow.size() > 0) {
            mPreSortOutData = mListShow.get(0);
            notifyItemClick();
        }
        mSortOutAdapter.setIntSelect(0);
        mSortOutAdapter.notifyDataSetChanged();
        //添加汇总
        if (mIntType == TYPE_WEIGHT) {
            if (mListShow.size() > 0
                    &&
                    (!TextUtils.isEmpty(mEtBasket.getText().toString().trim())
                            || !TextUtils.isEmpty(mEtGoodsNameOrId.getText().toString().trim())
                            || !TextUtils.isEmpty(mEtCustomGroup.getText().toString().trim()))) {
                BigDecimal amount = new BigDecimal(0.0);
                for (SortOutData sod : mListShow) {
                    amount = amount.add(sod.getCoverToKgQty());
                }
                mTvAmount.setText("汇总：".concat(BigDecimalUtil.toScaleStr(amount)).concat("kg"));
            } else {
                mTvAmount.setText("汇总：");
            }
            //稳定后 排序
            if (mStable) {
                String weight = !TextUtils.isEmpty(mEtShow.getText().toString().trim()) ? mEtShow.getText().toString().trim() : "0";
                sequenceListWeight(weight);
            }
        }
    }

    @Override
    public void onFinish(String key) {
        filterList();
    }

    @Override
    public void onQueryStockSuccess(PageData<Stock> result) {
        if (result != null) {
            List<Stock> stocks = result.getValues();
            if (stocks == null || stocks.size() == 0) {
                mListShow.clear();
                mSortOutAdapter.notifyDataSetChanged();
                ToastUtil.showShortToast(getContext(), "该周转筐没有商品");
                return;
            }
            int size = mListShow.size();
            //筛选出Stock的uuid跟SortOutData的uuid一样的商品
            Stock stock = stocks.get(0);
            ArrayList<SortOutData> listFilter = null;
            for (int i = 0; i < size; i++) {
                SortOutData data = mListShow.get(i);
                if (stock.getGoods().getUuid().equals(data.getGoods().getUuid())) {
                    if (listFilter == null) {
                        listFilter = new ArrayList<>();
                    }
                    listFilter.add(data);
                }
            }
            mListShow.clear();
            if (listFilter != null && listFilter.size() > 0) {
                mListShow.addAll(listFilter);
            }
            mSortOutAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onQueryStockFailed(int errorType, String failString) {
        ToastUtil.showShortToast(getContext(), failString);
    }

    /**
     * 商品列表适配器
     */
    private class SortOutAdapter extends CommonAdapter4Lv<SortOutData> {

        private int mIntSelect;
        private float mDim10Sp;

        private SortOutAdapter(Context context, List<SortOutData> data) {
            super(context, R.layout.item_sort_out_new, data);
            mDim10Sp = getResources().getDimension(R.dimen._10sp);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, SortOutData data, int position) {
            if (mIntSelect == position) {
                holder.setBackgroundRes(R.id.item_ll, R.color.color_ffe080);
            } else {
                holder.setBackgroundRes(R.id.item_ll, R.color.color_e7f0d5);
            }
            String unit = data.getGoodsUnit().getName();
            if (mIntType == TYPE_WEIGHT) {
                BigDecimal coverToKgQty = data.getCoverToKgQty();
                if (coverToKgQty != null) {
                    holder.setText(R.id.item_goods_weight, BigDecimalUtil.toScaleStr(coverToKgQty));
                    holder.setText(R.id.item_goods_weight_unit, "kg");
                }
                holder.setText(R.id.item_goods_weight_all, "总重量" + BigDecimalUtil.toScaleStr(data.getGoodsQty()) + unit);
                holder.setVisible(R.id.item_goods_weight_all, View.VISIBLE);
            } else if (mIntType == TYPE_COUNT) {
                holder.setText(R.id.item_goods_weight, BigDecimalUtil.toScaleStr(data.getGoodsQty().subtract(data.getHasStockOutQty())));
                holder.setText(R.id.item_goods_weight_unit, unit);
                holder.setText(R.id.item_goods_weight_all, "总数量" + BigDecimalUtil.toScaleStr(data.getGoodsQty()) + unit);
                holder.setVisible(R.id.item_goods_weight_all, View.VISIBLE);
            }

            BigDecimal hasStockOutQty = data.getHasStockOutQty();
            if (hasStockOutQty.doubleValue() != 0) {
                holder.setText(R.id.item_goods_weight_out, "已出" + BigDecimalUtil.toScaleStr(hasStockOutQty) + unit);
                holder.setVisible(R.id.item_goods_weight_out, View.VISIBLE);
            } else {
                holder.setText(R.id.item_goods_weight_out, "");
                holder.setVisible(R.id.item_goods_weight_out, View.INVISIBLE);
            }
            ScalableTextView tvName = holder.getView(R.id.item_goods_name);
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDim10Sp);
            tvName.setText(data.getGoods().getName());
            ScalableTextView tvCustomer = holder.getView(R.id.item_goods_customer);
            tvCustomer.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDim10Sp);
            holder.setText(R.id.item_goods_customer, data.getCustomer().getName());
        }

        private void setIntSelect(int intSelect) {
            mIntSelect = intSelect;
        }

    }

    /**
     * 云称接收回调
     */
    private String mPreWeight;

    @Override
    public void receive(Instrument.InsData data) {
        String weight = data.weight;
        if ((mIntType == TYPE_WEIGHT) && mEtShow != null) {
            mStable = data.stable;
            mEtShow.setText(weight);
            if (mStable && !weight.equals(mPreWeight)) {
                mPreWeight = weight;
                sequenceListWeight(weight);
            }
        }
    }

    /**
     * 根据称重数据进行排序，以称重数量差额的绝对值升序；
     */
    private ListComparator mListComparator;

    private void sequenceListWeight(String weight) {
        if (!TextUtils.isEmpty(weight) && mListShow.size() > 0) {
            double douWeight = Double.parseDouble(weight);
            if (mListComparator != null) {
                mListComparator.setDobWeight(douWeight);
            } else {
                mListComparator = new ListComparator(douWeight);
            }
            Collections.sort(mListShow, mListComparator);
            mSortOutAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public String getBaseTitle() {
        return "分拣";
    }
}
