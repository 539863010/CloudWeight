package xm.cloudweight;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.goods.GoodsCategory;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.inventory.InventoryRecord;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.stockout.StockOutRecord;
import com.xmzynt.storm.service.wms.stockout.StockOutType;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.PageData;

import java.math.BigDecimal;
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
import xm.cloudweight.bean.BeanSimilar;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.InputFragment;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.service.RequestDataService;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.BuglyUtil;
import xm.cloudweight.utils.bussiness.GetImageFile;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.MessageUtil;
import xm.cloudweight.utils.dao.DbRefreshUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.utils.dao.bean.DbRequestData;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.HistorySimilarPopWindow;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.OnDeleteListener;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

/**
 * @author wyh
 * @Description: 出库，调拨，盘点
 * @create 2017/10/25
 */
public class SimilarActivity extends BaseActivity implements
        AdapterView.OnItemSelectedListener, VideoFragment.OnInstrumentListener, OnDeleteListener {

    public static final String KEY_TYPE = "type";
    @BindView(R.id.ll_type)
    LinearLayout mLlType;
    DataSpinner<StockOutType> mSpTypeStockOut;
    DataSpinner<Warehouse> mSpWareHouseIn;
    @BindView(R.id.sp_goods_category)
    DataSpinner<GoodsCategory> mSpGoodsCategory;
    @BindView(R.id.sp_ware_house)
    DataSpinner<Warehouse> mSpWareHouse;
    @BindView(R.id.et_key_search)
    SearchAndFocusEditText mEtKeySearch;
    @BindView(R.id.et_tags)
    ScanEditText mEtTag;
    @BindView(R.id.lv_goods)
    ListView mLvGoods;
    @BindView(R.id.tv_goods)
    TextView mTvGoods;
    @BindView(R.id.btn_similar_history)
    Button mBtnHistory;
    private int mIntType;
    private List<Stock> mListShow = new ArrayList<>();
    private List<Stock> mListAll = new ArrayList<>();
    private GoodsAdapter mGoodsAdapter;
    private Stock mStock;
    private boolean loadWareHouse;
    private boolean loadCategory;
    private boolean loadList;
    private EditText mEtCount;
    private SearchAndFocusEditText mEtCurrentWeight;
    private SearchAndFocusEditText mEtLeather;
    private SearchAndFocusEditText mEtDeduct;
    private SearchAndFocusEditText mEtStockNum;
    private TextView mTvTitleStockNumUnit;
    private TextView mTvUnitCount;
    private EditText mEtAccumulate;
    private View mLlAccumulate;
    private Button mBtnAccumulate;
    private Button mBtnStockOut;
    private Button mBtnAllocate;
    private Button mBtnInventory;
    private VideoFragment mVideoFragment;
    private HistorySimilarPopWindow mSimilarPopWindow;
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private DbImageUpload mDbImageUpload;
    private boolean hasCancel;
    private InputFragment mInputFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_similar;
    }

    @Override
    protected void getDate() {
        super.getDate();
        mIntType = getIntent().getIntExtra(KEY_TYPE, 0);
    }

    @Override
    protected void initContentView() {
        //库存数
        View llStockNum = findViewById(R.id.ll_stock_num);
        TextView tvTitleStockNum = llStockNum.findViewById(R.id.tv_title);
        mTvTitleStockNumUnit = llStockNum.findViewById(R.id.tv_unit);
        tvTitleStockNum.setText("库存数");
        mEtStockNum = llStockNum.findViewById(R.id.ed_content);
        mEtStockNum.setEnabled(false);
        mEtStockNum.setBackground(null);
        mEtStockNum.setTextSize(getResources().getDimension(R.dimen._16sp));
        View llCurrentWeight = findViewById(R.id.ll_current_weight);
        TextView tvTitleCurrentWeight = llCurrentWeight.findViewById(R.id.tv_title);
        mEtCurrentWeight = llCurrentWeight.findViewById(R.id.ed_content);
        mEtCurrentWeight.setEnabled(false);
        mEtCurrentWeight.setBackground(null);
        mEtCurrentWeight.setTextSize(getResources().getDimension(R.dimen._16sp));
        tvTitleCurrentWeight.setText("当前重量");
        View llLeather = findViewById(R.id.ll_buckles_leather);
        TextView tvBucklesLeather = llLeather.findViewById(R.id.tv_title);
        mEtLeather = llLeather.findViewById(R.id.ed_content);
        tvBucklesLeather.setText("扣皮");
        View llDeduct = findViewById(R.id.ll_deduct_weight);
        TextView tvTitleDeductWeight = llDeduct.findViewById(R.id.tv_title);
        mEtDeduct = llDeduct.findViewById(R.id.ed_content);
        tvTitleDeductWeight.setText("扣重");
        View llCount = findViewById(R.id.ll_count);
        TextView tvCount = llCount.findViewById(R.id.tv_title);
        mTvUnitCount = llCount.findViewById(R.id.tv_unit);
        mEtCount = llCount.findViewById(R.id.ed_content);

        switch (mIntType) {
            case Common.SIMILAR_STOCKOUT:
                tvCount.setText("出库数量");
                initStockOutParams();
                break;
            case Common.SIMILAR_ALLOCATE:
                tvCount.setText("调拨数量");
                initAllocateParams();
                break;
            case Common.SIMILAR_CHECK:
                tvCount.setText("实际数量");
                mBtnHistory.setVisibility(View.GONE);
                initCheckParams();
                break;
            default:
                break;
        }

        mGoodsAdapter = new GoodsAdapter(this, mListShow);
        mLvGoods.setAdapter(mGoodsAdapter);
        mLvGoods.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //先清除数据
                clearContent(mListShow.get(i));
                setInfo();
                if (mInputFragment != null) {
                    mInputFragment.hide();
                }
            }
        });

        mSpGoodsCategory.setCustomItemSelectedListener(this);
        mSpWareHouse.setCustomItemSelectedListener(this);
        mEtTag.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key)) {
                    Warehouse warehouse = mSpWareHouse.getSelectedItem();
                    if (warehouse != null && !TextUtils.isEmpty(warehouse.getUuid())) {
                        showLoadingDialog(false);
                        scanByTraceCode(key, warehouse.getUuid());
                    }
                }
            }
        });
        mEtCurrentWeight.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                changeCountNum();
            }
        });
        mEtLeather.setOnInputFinishListener(mOnInputFinishListener);
        mEtDeduct.setOnInputFinishListener(mOnInputFinishListener);

        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        //设置小键盘
        mInputFragment = InputFragment.newInstance();
        mInputFragment.setEditTexts(mEtLeather, mEtDeduct, mEtCount);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mVideoFragment).add(R.id.container, mInputFragment)
                .hide(mInputFragment).show(mVideoFragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void loadDate() {
        DbRefreshUtil.refreshRegist(this, new DbRefreshUtil.onDbRefreshListener() {
            @Override
            public void onRefresh() {
                if (mSimilarPopWindow != null && mSimilarPopWindow.isShowing()) {
                    refreshHistoryList();
                }
            }
        });
        showLoadingDialog(true);
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(this);
        if (listWareHouse != null) {
            mSpWareHouse.setList(listWareHouse);
            if (mIntType == Common.SIMILAR_ALLOCATE) {
                mSpWareHouseIn.setList(listWareHouse);
            }
            loadWareHouse = true;
            filterList();
        } else {
            ToastUtil.showShortToast(this, "未获取到仓库列表信息");
        }
        getDropdownLeafCategory();
        queryStock();
    }

    private void queryStock() {
        try {
            long type = RequestDataService.TYPE_SIMILAR_QUERY_STOCK;
            Map<String, Object> params = new HashMap<>();
            params.put(Common.PAGE_STRING, Common.PAGE);
            params.put(Common.PAGE_SIZE_STRING, Common.PAGE_SIZE);
            params.put(Common.DEFAULT_PAGE_SIZE_STRING, Common.DEFAULT_PAGE_SIZE);
            params.put("basketCode", "");
            Warehouse warehouse = mSpWareHouse.getSelectedItem();
            if (warehouse != null) {
                params.put("warehouseUuid", warehouse.getUuid());
            } else {
                ToastUtil.showShortToast(getContext(), "请先设置配送仓信息");
                return;
            }
            getIRequestDataService().onGetDataListener(type, params, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, null));
                }

                @Override
                public void onError(long type, String message) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, message));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getDropdownLeafCategory() {
        try {
            long type = RequestDataService.TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY;
            getIRequestDataService().onGetDataListener(type, null, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, null));
                }

                @Override
                public void onError(long type, String message) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, message));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void scanByTraceCode(String traceCode, String warehouseUuid) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("traceCode", traceCode);
            params.put("warehouseUuid", warehouseUuid);
            getIRequestDataService().onGetDataListener(RequestDataService.TYPE_SCAN_BY_TRACE_CODE, params, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, null));
                }

                @Override
                public void onError(long type, String message) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, message));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbRefreshUtil.refreshUnRegist(this);
    }

    @OnClick({R.id.iv_similar_search, R.id.btn_clear_zero, R.id.btn_similar_history})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.iv_similar_search:
                showLoadingDialog(false);
                filterList();
                break;
            case R.id.btn_clear_zero:
                clearToZero();
                break;
            case R.id.btn_similar_history:
                showHistory();
                break;
            default:
                break;
        }
    }

    private void showHistory() {
        if (mSimilarPopWindow == null) {
            mSimilarPopWindow = new HistorySimilarPopWindow(getContext(), mIntType, mSpGoodsCategory);
            mSimilarPopWindow.setOnDeleteListener(this);
            mSimilarPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (hasCancel) {
                        mGoodsAdapter.notifyDataSetChanged();
                    }
                    hasCancel = false;
                }
            });
        }
        refreshHistoryList();
    }

    /**
     * 刷新历史列表
     */
    private void refreshHistoryList() {
        if (mIntType == Common.SIMILAR_CHECK) {
            return;
        }
        //获取数据库当天的数据
        List<DbImageUpload> daoList;
        if (mIntType == Common.SIMILAR_STOCKOUT) {
            daoList = getDbManager().getDbListSimilarStoreOutHistory();
        } else {
            daoList = getDbManager().getDbListSimilarAllocateHistory();
        }
        Collections.reverse(daoList);
        mListHistory.clear();
        mListHistory.addAll(daoList);
        mSimilarPopWindow.show();
        mSimilarPopWindow.notify(mListHistory);
    }

    @Override
    public void delete(DbImageUpload data) {
        mDbImageUpload = data;
        int type = data.getType();
        String stockOutUuid = data.getStockOutUuid();
        requestCancel(type, stockOutUuid);
    }

    private void requestCancel(int type, String uuid) {
        showLoadingDialog(false);
        if (!TextUtils.isEmpty(uuid)) {
            cancelSimilar(uuid, type);
        } else {
            onCancelSuccess();
        }
    }

    private void cancelSimilar(String uuid, int type) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("type", type);
            params.put("uuid", uuid);
            getIRequestDataService().onGetDataListener(RequestDataService.TYPE_SIMILAR_CANCEL, params, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, null));
                }

                @Override
                public void onError(long type, String message) throws RemoteException {
                    mRequestData.sendMessage(MessageUtil.create(type, message));
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onCancelSuccess() {
        //先刷新后删除数据库
        refreshCancel();
        getDbManager().deleteDbImageUpload(mDbImageUpload);
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销成功");
        hasCancel = true;
        dismissLoadingDialog();
        mDbImageUpload = null;
    }

    private void refreshCancel() {
        String cancelUuid = null;
        BigDecimal quantity = null;
        int type = mDbImageUpload.getType();
        if (type == Common.DbType.TYPE_STORE_OUT) {
            //出库
            StockOutRecord stockOutRecord = GsonUtil.getGson().fromJson(mDbImageUpload.getLine(), StockOutRecord.class);
            cancelUuid = stockOutRecord.getStockUuid();
            quantity = stockOutRecord.getQuantity();
        } else if (type == Common.DbType.TYPE_ALLOCATE) {
            AllocateRecord allocateRecord = GsonUtil.getGson().fromJson(mDbImageUpload.getLine(), AllocateRecord.class);
            cancelUuid = allocateRecord.getSourceStockUuid();
            quantity = allocateRecord.getAllocateQty();
        }
        if (TextUtils.isEmpty(cancelUuid) || quantity == null) {
            return;
        }
        for (Stock stock : mListAll) {
            if (cancelUuid.equals(stock.getUuid())) {
                BigDecimal amount = stock.getAmount();
                stock.setAmount(amount.add(quantity));
                break;
            }
        }
    }

    /**
     * 拍照Handler
     */
    private Handler mHandlerShotPic = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showLoadingDialog(false);
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
        BigDecimal leather = getEtToBigdecimal(mEtLeather);
        BigDecimal deduct = getEtToBigdecimal(mEtDeduct);
        BigDecimal count = getEtToBigdecimal(mEtCount);

        DbImageUpload dbImageUpload = new DbImageUpload();
        dbImageUpload.setImagePath(imagePath);
        Date date = new Date();
        dbImageUpload.setDate(DateUtils.getTime(date));
        dbImageUpload.setOperatime(DateUtils.getTime2(date));
        if (mIntType == Common.SIMILAR_STOCKOUT) {
            //出库
            StockOutRecord record = BeanSimilar.createStoreOutRecord(getContext(),
                    mStock,
                    mSpTypeStockOut,
                    count,
                    leather,
                    deduct);
            record.setImages(Arrays.asList(GetImageFile.getName(imagePath)));
            dbImageUpload.setLine(GsonUtil.getGson().toJson(record));
            dbImageUpload.setType(Common.DbType.TYPE_STORE_OUT);
            dbImageUpload.setImagePath(imagePath);
            getDbManager().insertDbImageUpload(dbImageUpload);
            showSuccessResult("出库");
            setBtnEnable(mBtnStockOut);
        } else if (mIntType == Common.SIMILAR_ALLOCATE) {
            //调拨
            AllocateRecord allocateRecord = BeanSimilar.createAllocateRecord(getContext(),
                    mStock,
                    mSpWareHouseIn,
                    count,
                    leather,
                    deduct);
            allocateRecord.setImages(Arrays.asList(imagePath));
            dbImageUpload.setLine(GsonUtil.getGson().toJson(allocateRecord));
            dbImageUpload.setType(Common.DbType.TYPE_ALLOCATE);
            dbImageUpload.setImagePath(imagePath);
            getDbManager().insertDbImageUpload(dbImageUpload);
            showSuccessResult("调拨");
            setBtnEnable(mBtnAllocate);
        } else {
            //盘点
            InventoryRecord inventoryRecord = BeanSimilar.createInventoryRecord(getContext(),
                    mStock,
                    count);
            inventoryRecord.setImages(Arrays.asList(imagePath));
            dbImageUpload.setLine(GsonUtil.getGson().toJson(inventoryRecord));
            dbImageUpload.setType(Common.DbType.TYPE_CHECK);
            dbImageUpload.setImagePath(imagePath);
            getDbManager().insertDbImageUpload(dbImageUpload);
            showSuccessResult("盘点");
            setBtnEnable(mBtnInventory);
        }
        dismissLoadingDialog();
    }

    /**
     * 初始化出库布局
     */
    private void initStockOutParams() {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_type_stock_out, null);
        mSpTypeStockOut = layout.findViewById(R.id.sp_type_stock_out);
        mSpTypeStockOut.setTitleColor(R.color.color_135c31);
        mLlType.addView(layout);
        mBtnStockOut = layout.findViewById(R.id.btn_store_out);
        mBtnStockOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkToRequest("出库数量不能为空")) {
                    mBtnStockOut.setEnabled(false);
                    if (mVideoFragment.isLight()) {
                        mVideoFragment.screenshot(mHandlerShotPic);
                    } else {
                        showLoadingDialog(false);
                        shotResult(null);
                    }
                }
            }
        });
        //获取出库类别
        ArrayList<StockOutType> list = new ArrayList<>();
        list.add(StockOutType.pickingOut);
        list.add(StockOutType.lossOut);
        list.add(StockOutType.other);
        mSpTypeStockOut.setList(list);
    }

    /**
     * 初始化调拨布局
     */
    private void initAllocateParams() {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_type_allocate, null);
        //要调出的仓库
        mSpWareHouseIn = layout.findViewById(R.id.sp_ware_house_in);
        mSpWareHouseIn.setTitleColor(R.color.color_135c31);
        mLlType.addView(layout);
        mBtnAllocate = layout.findViewById(R.id.btn_allocate);
        mBtnAllocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Warehouse warehouse = mSpWareHouse.getSelectedItem();
                Warehouse wareHouseOut = mSpWareHouseIn.getSelectedItem();
                if (warehouse == null || wareHouseOut == null
                        || TextUtils.isEmpty(warehouse.getName()) || TextUtils.isEmpty(wareHouseOut.getName())) {
                    return;
                }
                if (warehouse.getName().equals(wareHouseOut.getName())
                        && warehouse.getCode().equals(wareHouseOut.getCode())) {
                    ToastUtil.showShortToast(getContext(), "调入仓库不能选择所在仓库");
                    return;
                }
                if (checkToRequest("调拨数量不能为空")) {
                    mBtnAllocate.setEnabled(false);
                    if (mVideoFragment.isLight()) {
                        mVideoFragment.screenshot(mHandlerShotPic);
                    } else {
                        showLoadingDialog(false);
                        shotResult(null);
                    }
                }
            }
        });
    }

    /**
     * 初始化盘点布局
     */
    private void initCheckParams() {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_type_check, null);
        //累计重量布局
        mLlAccumulate = findViewById(R.id.ll_weight_accumulate);
        mLlAccumulate.setVisibility(View.VISIBLE);
        mEtAccumulate = (EditText) mLlAccumulate.findViewById(R.id.ed_content);
        mEtAccumulate.setEnabled(false);
        mEtAccumulate.setBackground(null);
        mEtAccumulate.setTextColor(getResources().getColor(R.color.black));
        mEtAccumulate.setTextSize(getResources().getDimension(R.dimen._12sp));
        TextView tvTitleAccumulate = mLlAccumulate.findViewById(R.id.tv_title);
        tvTitleAccumulate.setText("累计重量");
        mLlType.addView(layout);
        mBtnAccumulate = layout.findViewById(R.id.btn_add_weight);
        mBtnAccumulate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStable) {
                    BigDecimal accumulate = getEtToBigdecimal(mEtAccumulate);
                    BigDecimal weight = getEtToBigdecimal(mEtCurrentWeight);
                    BigDecimal leather = getEtToBigdecimal(mEtLeather);
                    BigDecimal deduct = getEtToBigdecimal(mEtDeduct);
                    mEtAccumulate.setText(BigDecimalUtil.toScaleStr(weight.subtract(leather).subtract(deduct).add(accumulate)));
                } else {
                    ToastUtil.showShortToast(getContext(), "请待称稳定");
                }
            }
        });
        mBtnInventory = layout.findViewById(R.id.btn_inventory);
        mBtnInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkToRequest("实际数量不能为空")) {
                    mBtnInventory.setEnabled(false);
                    if (mVideoFragment.isLight()) {
                        mVideoFragment.screenshot(mHandlerShotPic);
                    } else {
                        showLoadingDialog(false);
                        shotResult(null);
                    }
                }
            }
        });
    }

    private void setBtnEnable(View btnClick) {
        if (!btnClick.isEnabled()) {
            btnClick.setEnabled(true);
        }
    }

    private boolean checkToRequest(String toastStr) {
        if (mStock != null) {
            String strCount = mEtCount.getText().toString().trim();
            if (!TextUtils.isEmpty(strCount)) {
                return true;
            } else {
                ToastUtil.showShortToast(getContext(), toastStr);
            }
        } else {
            ToastUtil.showShortToast(getContext(), "请选择商品");
        }
        return false;
    }

    /**
     * 处理成功后的结果
     */
    private void showSuccessResult(String typeStr) {
        if (!mListShow.contains(mStock)) {
            return;
        }
        int indexOf = mListShow.indexOf(mStock);
        Stock stock = mListShow.get(indexOf);
        //剩余数量
        BigDecimal weightSurplus;
        //已减数量
        BigDecimal countSub = new BigDecimal(mEtCount.getText().toString().trim());
        BigDecimal weightCoefficient = mStock.getWeightCoefficient();
        if (weightCoefficient != null) {
            //反算单位
            if (mIntType == Common.SIMILAR_CHECK) {
                //如果是盘点， 数量要双倍
                weightSurplus = countSub.divide(weightCoefficient, ROUND_HALF_EVEN);
            } else {
                weightSurplus = mStock.getAmount().subtract(countSub.divide(weightCoefficient, ROUND_HALF_EVEN));
            }
        } else {
            if (mIntType == Common.SIMILAR_CHECK) {
                weightSurplus = countSub;
            } else {
                weightSurplus = mStock.getAmount().subtract(countSub);
            }
        }
        stock.setAmount(weightSurplus);
        //如果数量为0 ，从列表删除
        if (weightSurplus.doubleValue() == 0) {
            mListShow.remove(indexOf);
        }
        mGoodsAdapter.notifyDataSetChanged();
        clearContent(null);
        ToastUtil.showShortToast(getContext(), typeStr + "成功");
        mEtTag.requestFocus();
    }

    /**
     * 设置商品信息
     */
    private void setInfo() {
        BigDecimal weightCoefficient = mStock.getWeightCoefficient();
        if (mIntType == Common.SIMILAR_CHECK) {
            if (weightCoefficient != null) {
                mLlAccumulate.setVisibility(View.VISIBLE);
                mBtnAccumulate.setVisibility(View.VISIBLE);
            } else {
                mBtnAccumulate.setVisibility(View.GONE);
                mLlAccumulate.setVisibility(View.GONE);
            }
        }
        UCN goods = mStock.getGoods();
        mTvGoods.setText(goods != null && !TextUtils.isEmpty(goods.getName()) ? goods.getName() : "");
        BigDecimal amount = mStock.getAmount();
        if (weightCoefficient != null) {
            //重量单位  amount * 系数
            String weight = BigDecimalUtil.toScaleStr(amount.multiply(weightCoefficient));
            mEtStockNum.setText(weight);
            mTvTitleStockNumUnit.setText("kg");
            setEtWeightCurrent(weight);
            mTvUnitCount.setText("kg");
            mEtLeather.setEnabled(true);
            mEtDeduct.setEnabled(true);
            mEtCount.setEnabled(false);
            mEtCount.setText(mEtCurrentWeight.getText().toString().trim());
        } else {
            //数量单位   显示 数量+单位
            String unit = mStock.getGoodsUnit().getName();
            mEtStockNum.setText(BigDecimalUtil.toScaleStr(amount));
            mTvTitleStockNumUnit.setText(unit);
            mTvUnitCount.setText(unit);
            mEtLeather.setEnabled(false);
            mEtDeduct.setEnabled(false);
            mEtCount.setEnabled(true);
            mEtCount.setText("");
        }
    }

    /**
     * 清空文本内容
     */
    private void clearContent(Stock stock) {
        mStock = stock;

        mTvGoods.setText("");
        mEtStockNum.setText("");
        mTvTitleStockNumUnit.setText("");

        setEtWeightCurrent("");
        mEtLeather.setText("");
        mEtDeduct.setText("");
        mEtCount.setText("");

        switch (mIntType) {
            case Common.SIMILAR_STOCKOUT:
                mSpTypeStockOut.setSelection(0);
                break;
            case Common.SIMILAR_ALLOCATE:
                mSpWareHouseIn.setSelection(0);
                break;
            case Common.SIMILAR_CHECK:
                break;
            default:
                break;
        }

    }

    /**
     * 过滤列表
     */
    private void filterList() {
        if (!loadWareHouse || !loadCategory || !loadList) {
            return;
        }
        if (mListAll.size() == 0) {
            mGoodsAdapter.notifyDataSetChanged();
            dismissLoadingDialog();
            return;
        }
        Warehouse warehouse = mSpWareHouse.getSelectedItem();
        ArrayList<Stock> listWarehouseName = new ArrayList<>();
        if (warehouse != null && !TextUtils.isEmpty(warehouse.getName())) {
            String warehouseName = warehouse.getName();
            for (Stock stock : mListAll) {
                UCN stockWarehouse = stock.getWarehouse();
                if (stockWarehouse != null && stockWarehouse.getName().equals(warehouseName)) {
                    listWarehouseName.add(stock);
                }
            }
        } else {
            listWarehouseName.addAll(mListAll);
        }

        GoodsCategory category = mSpGoodsCategory.getSelectedItem();
        ArrayList<Stock> listCategoryName = new ArrayList<>();
        if (category != null && !TextUtils.isEmpty(category.getName()) && !category.getName().equals("全部")) {
            String categoryName = category.getName();
            for (Stock stock : listWarehouseName) {
                IdName goodsCategory = stock.getGoodsCategory();
                if ((goodsCategory != null && goodsCategory.getName().equals(categoryName))) {
                    listCategoryName.add(stock);
                }
            }
        } else {
            listCategoryName.addAll(listWarehouseName);
        }

        String key = mEtKeySearch.getText().toString().trim();
        ArrayList<Stock> listKeyName = new ArrayList<>();
        if (!TextUtils.isEmpty(key)) {
            for (Stock stock : listCategoryName) {
                UCN goods = stock.getGoods();
                if (goods != null && goods.getName().contains(key)) {
                    listKeyName.add(stock);
                }
            }
        } else {
            listKeyName.addAll(listCategoryName);
        }
        mListShow.clear();
        mListShow.addAll(listKeyName);
        mGoodsAdapter.notifyDataSetChanged();
        dismissLoadingDialog();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_ware_house:
            case R.id.sp_goods_category:
                showLoadingDialog(false);
                //清除列表信息
                mListShow.clear();
                mGoodsAdapter.notifyDataSetChanged();
                clearContent(null);
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
     * 文本监听
     */
    private onInputFinishListener mOnInputFinishListener = new onInputFinishListener() {
        @Override
        public void onFinish(String key) {
            changeCountNum();
        }
    };

    /**
     * 改变数量，重量
     */
    private void changeCountNum() {
        String strCurrentWeight = mEtCurrentWeight.getText().toString().trim();
        if (TextUtils.isEmpty(strCurrentWeight)) {
            return;
        }
        BigDecimal currentWeight = new BigDecimal(strCurrentWeight);
        String strLeather = mEtLeather.getText().toString().trim();
        BigDecimal leather = new BigDecimal(!TextUtils.isEmpty(strLeather) ? strLeather : "0");
        String strDeduct = mEtDeduct.getText().toString().trim();
        BigDecimal deduct = new BigDecimal(!TextUtils.isEmpty(strDeduct) ? strDeduct : "0");
        mEtCount.setText(BigDecimalUtil.toScaleStr(currentWeight.subtract(leather).subtract(deduct)));
    }

    /**
     * 商品列表适配器
     */
    private class GoodsAdapter extends CommonAdapter4Lv<Stock> {

        private GoodsAdapter(Context context, List<Stock> data) {
            super(context, R.layout.item_similar, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, Stock stock, int position) {
            holder.setText(R.id.tv_goods_name, stock.getGoods().getName());
            holder.setText(R.id.tv_goods_weight, BigDecimalUtil.toScaleStr(stock.getAmount()) + stock.getGoodsUnit().getName());
        }
    }

    private boolean mStable;
    private String mPreWeight;

    @Override
    public void receive(Instrument.InsData data) {
        if (mEtCurrentWeight == null) {
            return;
        }
        if (data == null) {
            setEtWeightCurrent("");
        } else {
            mStable = data.stable;
            if (mStock != null && mStock.getWeightCoefficient() != null) {
                mEtCurrentWeight.setText(data.weight);
                if (data.stable && !TextUtils.equals(data.weight, mPreWeight)) {
                    BigDecimal leather = getEtToBigdecimal(mEtLeather);
                    BigDecimal deduct = getEtToBigdecimal(mEtDeduct);
                    BigDecimal weight = getEtToBigdecimal(mEtCurrentWeight);
                    mEtCount.setText(BigDecimalUtil.toScaleStr(weight.subtract(leather).subtract(deduct)));
                }
            }
            mPreWeight = data.weight;
        }
    }

    private void setEtWeightCurrent(String weight) {
        mEtCurrentWeight.setText(weight);
        mPreWeight = weight;
    }

    private BigDecimal getEtToBigdecimal(EditText et) {
        String content = et.getText().toString().trim();
        return new BigDecimal(!TextUtils.isEmpty(content) ? content : "0");
    }

    @Override
    public String getBaseTitle() {
        if (mIntType == Common.SIMILAR_STOCKOUT) {
            return "出库";
        } else if (mIntType == Common.SIMILAR_ALLOCATE) {
            return "调拨";
        } else if (mIntType == Common.SIMILAR_CHECK) {
            return "盘点";
        } else {
            return null;
        }
    }

    private Handler mRequestData = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long type = MessageUtil.getType(msg);
            if (type == RequestDataService.TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY) {
                getDropDownLeafCategorySuccess(type);
            } else if (type == RequestDataService.TYPE_SIMILAR_GET_DROPDOWN_LEAFCATEGORY_FAILED) {
                String failString = MessageUtil.getObj(msg);
                ToastUtil.showShortToast(getContext(), failString);
                dismissLoadingDialog();
            } else if (type == RequestDataService.TYPE_SIMILAR_QUERY_STOCK) {
                queryStockSuccess(type);
            } else if (type == RequestDataService.TYPE_SIMILAR_QUERY_STOCK_FAILED) {
                ToastUtil.showShortToast(getContext(), MessageUtil.getObj(msg));
                dismissLoadingDialog();
            } else if (type == RequestDataService.TYPE_SIMILAR_CANCEL) {
                onCancelSuccess();
            } else if (type == RequestDataService.TYPE_SIMILAR_CANCEL_FAILED) {
                dismissLoadingDialog();
                mDbImageUpload = null;
                ToastUtil.showShortToast(getContext(), MessageUtil.getObj(msg));
            } else if (type == RequestDataService.TYPE_SCAN_BY_TRACE_CODE) {
                scanByTraceCodeSuccess(type);
                mEtTag.setText("");
            } else if (type == RequestDataService.TYPE_SCAN_BY_TRACE_CODE_FAILED) {
                ToastUtil.showShortToast(getContext(), MessageUtil.getObj(msg));
                dismissLoadingDialog();
                mEtTag.setText("");
            }
            return false;
        }
    });

    public void scanByTraceCodeSuccess(long type) {
        dismissLoadingDialog();
        mEtTag.setText("");
        List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
        String json = dbRequestData.get(0).getData();
        List<Stock> result = GsonUtil.getGson().fromJson(json, new TypeToken<List<Stock>>() {
        }.getType());
        if (result == null || result.size() == 0) {
            ToastUtil.showShortToast(getContext(), "暂无数据");
            return;
        }
        //若有传仓库uuid则返回的list为1调或空，无不传，则返回多条或空
        if (result.size() == 1) {
            clearContent(result.get(0));
            setInfo();
        }
    }

    private void getDropDownLeafCategorySuccess(long type) {
        List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
        String json = dbRequestData.get(0).getData();
        List<GoodsCategory> result = GsonUtil.getGson().fromJson(json, new TypeToken<List<GoodsCategory>>() {
        }.getType());
        GoodsCategory category = new GoodsCategory();
        category.setName("全部");
        result.add(0, category);
        mSpGoodsCategory.setList(result);
        loadCategory = true;
        filterList();
    }

    public void queryStockSuccess(long type) {
        try {
            List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
            String json = dbRequestData.get(0).getData();
            PageData<Stock> result = GsonUtil.getGson().fromJson(json, new TypeToken<PageData<Stock>>() {
            }.getType());
            if (result == null) {
                dismissLoadingDialog();
                return;
            }
            List<Stock> values = result.getValues();
            if (values == null || values.size() == 0) {
                clearContent(null);
                dismissLoadingDialog();
                return;
            }
            if (values.size() == 1) {
                //通过扫描获取的直接设置
                mEtTag.setText("");
                clearContent(values.get(0));
                setInfo();
                dismissLoadingDialog();
            } else {
                mListAll.clear();
                mListAll.addAll(values);
                loadList = true;
                filterList();
            }
        } catch (Exception e) {
            BuglyUtil.uploadCrash(e);
        }
    }

}
