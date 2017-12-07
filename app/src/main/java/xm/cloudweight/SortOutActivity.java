package xm.cloudweight;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.PopupWindow;
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
import xm.cloudweight.utils.IsBottomUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.EtMaxLengthUtil;
import xm.cloudweight.utils.bussiness.FilterUtil;
import xm.cloudweight.utils.bussiness.ListComparator;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
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
        CommImpl.OnQueryStockListener
        , SortOutImpl.OnGetSortOutListListener
        , SortOutImpl.OnCancelSortOutListener
        , AdapterView.OnItemSelectedListener
        , SortOutHistoryPopWindow.OnDeleteListener, onInputFinishListener, VideoFragment.OnInstrumentListener, AbsListView.OnScrollListener, PopupWindow.OnDismissListener {

    public static final int TYPE_WEIGHT = 0;
    public static final int TYPE_COUNT = 1;
    public static final int DEFAULT_PAGE_SIZE = 0;
    public static final int PAGE_SIZE = 0;
    public static final int PAGE = 0;
    public static final int UPLOAD_PAGE_SIZE = 50;

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
    @BindView(R.id.btn_request)
    Button mBtnRequest;
    private List<SortOutData> mListShow = new ArrayList<>();
    private ArrayList<SortOutData> mListFilter = new ArrayList<>();
    private List<SortOutData> mListAllWeight = new ArrayList<>();
    private List<SortOutData> mListAllCount = new ArrayList<>();
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private SortOutAdapter mSortOutAdapter;
    private int mIntType = TYPE_WEIGHT;
    private boolean loadCustomerLevel;
    private boolean loadWeightSuccess;
    private boolean loadCountSuccess;
    private boolean hasCancelSortOut;
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

    @SuppressLint("ClickableViewAccessibility")
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
        mGvSortOut.setOnScrollListener(this);
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
                    SortOutPresenter.getSourOutList(getActivity(), TYPE_WEIGHT, PAGE, PAGE_SIZE, DEFAULT_PAGE_SIZE, mBtnDate.getText().toString());
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
        getLocalInfo();
        mBtnSortOutCount.setSelected(false);
        mBtnSortOutWeight.setSelected(true);
        //设置当前日期
        String currentData = DateUtils.StringData();
        mBtnDate.setText(currentData);
    }

    private void getLocalInfo() {
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(this);
        if (listWareHouse != null) {
            mSpWareHouse.setList(listWareHouse);
        } else {
            ToastUtil.showShortToast(this, "未获取到仓库列表信息");
        }

        List<CustomerLevel> listCustomerLevel = LocalSpUtil.getListCustomerLevel(this);
        if (listCustomerLevel != null) {
            if (listCustomerLevel.size() > 0) {
                CustomerLevel e = new CustomerLevel();
                e.setName("全部");
                listCustomerLevel.add(0, e);
                mSpCustomersLevel.setList(listCustomerLevel);
                loadCustomerLevel = true;
                filterList();
            }
        } else {
            ToastUtil.showShortToast(this, "未获取到用户等级列表信息");
        }

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

    @OnClick({R.id.btn_sort_out_weight, R.id.btn_sort_out_count, R.id.btn_request, R.id.btn_sort_out_history, R.id.btn_sort_out, R.id.btn_date})
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
                mIntType = TYPE_WEIGHT;
                setClick("称重数：", false, false, true);
                break;
            }
            case R.id.btn_sort_out_count: {
                if (mIntType == TYPE_COUNT) {
                    return;
                }
                mIntType = TYPE_COUNT;
                setClick("出库数量：", true, true, false);
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
            case R.id.btn_request:
                refreshSortOutList();
                break;
            default:
                break;
        }
    }

    private void refreshSortOutList() {
        String time = mBtnDate.getText().toString().trim();
        if (!TextUtils.isEmpty(time)) {
            mBtnRequest.setEnabled(false);
            dismissP();
            showP();
            loadWeightSuccess = false;
            loadCountSuccess = false;
            mListAllWeight.clear();
            mListAllCount.clear();
            mListFilter.clear();
            mListShow.clear();
            mSortOutAdapter.notifyDataSetChanged();
            SortOutPresenter.getSourOutList(this, TYPE_WEIGHT, PAGE, PAGE_SIZE, DEFAULT_PAGE_SIZE, time);
            SortOutPresenter.getSourOutList(this, TYPE_COUNT, PAGE, PAGE_SIZE, DEFAULT_PAGE_SIZE, time);
        }
    }

    private void setClick(String type, boolean showEnable, boolean countSelect, boolean weightSelect) {
        mTvType.setText(type);
        mEtShow.setText("");
        mEtShow.setEnabled(showEnable);
        mBtnSortOutCount.setSelected(countSelect);
        mBtnSortOutWeight.setSelected(weightSelect);
        mListFilter.clear();
        mListShow.clear();
        mSortOutAdapter.notifyDataSetChanged();
        filterList();
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
            // 生成追溯码
            mPreSortOutData.setPlatformTraceCode(Common.getPlatformTraceCode());
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
                removeCurrentItem(mListShow);
                removeCurrentItem(mListFilter);
                if (mIntType == TYPE_WEIGHT) {
                    removeCurrentItem(mListAllWeight);
                } else {
                    removeCurrentItem(mListAllCount);
                }
            } else {
                //显示已出 数量
                if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
                    mPreSortOutData.setCoverToKgQty(mPreSortOutData.getCoverToKgQty().subtract(stockOutQty.multiply(unitCoefficient)));
                }
                mPreSortOutData.setHasStockOutQty(outAmount);
            }
            //打印标签
            printer(unitCoefficient);
            //先打印后清除数据
            mPreSortOutData.setStockOutQty(null);
            mPreSortOutData.setWarehouse(null);
            mPreSortOutData.setPlatformTraceCode(null);
            ToastUtil.showShortToast(getContext(), "分拣成功");
            //默认设置第一个
            mSortOutAdapter.setIntSelect(0);
            mSortOutAdapter.notifyDataSetChanged();
            if (mListShow.size() > 0) {
                mPreSortOutData = mListShow.get(0);
                notifyItemClick();
            }
        }
    }

    /**
     * 删除当前选中的item
     */
    private void removeCurrentItem(List<SortOutData> list) {
        int indexShow = getListIndex(list);
        if (indexShow != -1) {
            list.remove(indexShow);
        }
    }

    /**
     * 获取列表中与当前选中项相同SourceBillLineUuid的下标
     */
    private int getListIndex(List<SortOutData> list) {
        int index = -1;
        if (list != null && mPreSortOutData != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                SortOutData sortOutData = list.get(i);
                if (sortOutData.getSourceBillLineUuid().equals(mPreSortOutData.getSourceBillLineUuid())) {
                    return i;
                }
            }
        }
        return index;
    }

    private void printer(BigDecimal unitCoefficient) {
        // 打印标签
        if (mPrinterView == null) {
            mPrinterView = (PrinterView) findViewById(R.id.pv);
        }
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
        beanPrinter.setCode(mPreSortOutData.getPlatformTraceCode());
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
        }
    };

    /**
     * 弹窗
     */
    private void toHistory() {
        if (mHistoryPopWindow == null) {
            mHistoryPopWindow = new SortOutHistoryPopWindow(this, mBtnSortOutHistory);
            mHistoryPopWindow.setOnDeleteListener(this);
            mHistoryPopWindow.setOnDismissListener(this);
        }
        refreshHistoryList();
    }

    @Override
    public void onDismiss() {
        if (hasCancelSortOut) {
            refreshSortOutList();
        }
        hasCancelSortOut = false;
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
            hasCancelSortOut = true;
        }
    }

    @Override
    public void getSortOutListSuccess(int type, List<SortOutData> data) {
        if (type == TYPE_WEIGHT) {
            loadWeightSuccess = true;
            mListAllWeight.clear();
            mListAllWeight.addAll(data);
        } else if (type == TYPE_COUNT) {
            loadCountSuccess = true;
            mListAllCount.clear();
            mListAllCount.addAll(data);
        }
        if (loadWeightSuccess && loadCountSuccess) {
            mBtnRequest.setEnabled(true);
            //设置客户列表
            getListCustomer();
            //筛选条件
            filterList();
            dismissP();
        }
    }

    /**
     * 根据重量数据+数量数据  过滤出客户列表
     */
    private void getListCustomer() {
        List<SortOutData> mListAll = new ArrayList<>();
        mListAll.addAll(mListAllWeight);
        mListAll.addAll(mListAllCount);
        List<MerchantCustomer> list = new ArrayList<>();
        MerchantCustomer e = new MerchantCustomer();
        Customer customerAll = new Customer();
        customerAll.setName("全部");
        e.setCustomer(customerAll);
        list.add(0, e);
        for (SortOutData sortOutData : mListAll) {
            IdName customer = sortOutData.getCustomer();
            boolean hasAdd = false;
            for (MerchantCustomer merchantCustomer : list) {
                Customer merchantCustomerCustomer = merchantCustomer.getCustomer();
                //去重
                if (merchantCustomerCustomer.getName().equals(customer.getName())
                        &&
                        merchantCustomerCustomer.getUuid().equals(customer.getId())) {
                    hasAdd = true;
                }
            }
            if (!hasAdd) {
                MerchantCustomer merchantCustomerNew = new MerchantCustomer();
                Customer customerNew = new Customer();
                customerNew.setName(customer.getName());
                customerNew.setUuid(customer.getId());
                merchantCustomerNew.setCustomer(customerNew);
                list.add(merchantCustomerNew);
            }
        }
        mSpCustomers.setList(list);
    }

    @Override
    public void getSortOutListFailed(int type, String message) {
        if (type == TYPE_WEIGHT) {
            loadWeightSuccess = false;
        } else {
            loadCountSuccess = false;
        }
        if (!loadWeightSuccess && !loadCountSuccess) {
            dismissP();
            mBtnRequest.setEnabled(true);
        }
        ToastUtil.showShortToast(getContext(), message);
    }

    @Override
    public void onCancelSortOutSuccess(SortOutData data) {
        getDbManager().deleteDbImageUpload(mDbImageUpload);
        mDbImageUpload = null;
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销分拣成功");
        hasCancelSortOut = true;
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
        if (!loadCountSuccess || !loadWeightSuccess || !loadCustomerLevel) {
            return;
        }

        mTvAmount.setText("汇总：");

        if ((mIntType == TYPE_WEIGHT && (mListAllWeight == null || mListAllWeight.size() == 0))
                ||
                (mIntType == TYPE_COUNT && (mListAllCount == null || mListAllCount.size() == 0))) {
            mListFilter.clear();
            mListShow.clear();
            scrollToItem(0);
            return;
        }

        //逐级删除不符合的数据
        ArrayList<SortOutData> filter;
        if (mIntType == TYPE_WEIGHT) {
            //重量
            filter = FilterUtil.filter(mListAllWeight, mSpCustomersLevel, mSpCustomers, mEtGoodsNameOrId, mEtCustomGroup);
        } else {
            //数量
            filter = FilterUtil.filter(mListAllCount, mSpCustomersLevel, mSpCustomers, mEtGoodsNameOrId, mEtCustomGroup);
        }
        mListFilter.clear();
        mListFilter.addAll(filter);

        mListShow.clear();
        if (mListFilter.size() > UPLOAD_PAGE_SIZE) {
            mListShow.addAll(mListFilter.subList(0, UPLOAD_PAGE_SIZE));
        } else {
            mListShow.addAll(mListFilter);
        }
        if (mListShow.size() > 0) {
            mPreSortOutData = mListShow.get(0);
            notifyItemClick();
        }
        scrollToItem(0);
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
                scrollToItem(0);
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
            scrollToItem(0);
        }
    }

    @Override
    public void onQueryStockFailed(int errorType, String failString) {
        ToastUtil.showShortToast(getContext(), failString);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (IsBottomUtil.isBottom(mGvSortOut)) {
            int sizeFilter = mListFilter.size();
            int sizeShow = mListShow.size();
            if (sizeFilter > sizeShow) {
                int endSize = sizeShow + UPLOAD_PAGE_SIZE;
                if (sizeFilter > endSize) {
                    mListShow.addAll(mListFilter.subList(sizeShow, endSize));
                } else {
                    mListShow.addAll(mListFilter.subList(sizeShow, sizeFilter));
                }
                mSortOutAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 商品列表适配器
     */
    private class SortOutAdapter extends CommonAdapter4Lv<SortOutData> {

        private int mIntSelect;
        private float mDim10Sp;

        private SortOutAdapter(Context context, List<SortOutData> data) {
            super(context, R.layout.item_sort_out, data);
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
            //设置备注
            holder.setText(R.id.item_goods_remark, data.getRemark());
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
            scrollToItem(0);
        }
    }

    private void scrollToItem(int itemPosition) {
        mSortOutAdapter.setIntSelect(itemPosition);
        mSortOutAdapter.notifyDataSetChanged();
        mGvSortOut.smoothScrollToPosition(itemPosition);
    }

    @Override
    public String getBaseTitle() {
        return "分拣";
    }
}
