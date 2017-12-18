package xm.cloudweight;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.BeanSimilar;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.impl.CommImpl;
import xm.cloudweight.impl.SimilarImpl;
import xm.cloudweight.presenter.CommPresenter;
import xm.cloudweight.presenter.SimilarPresenter;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.EtMaxLengthUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.SimilarPopWindow;
import xm.cloudweight.widget.adapter.BasketAdapter;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

/**
 * @author wyh
 * @Description: 出库，调拨，盘点
 * @create 2017/10/25
 */
public class SimilarActivity extends BaseActivity implements SimilarImpl.OnGetDropdownLeafCategoryListener
        , CommImpl.OnQueryStockListener
        , AdapterView.OnItemSelectedListener, VideoFragment.OnInstrumentListener {

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
    @BindView(R.id.et_basket)
    ScanEditText mEtBasket;
    @BindView(R.id.lv_goods)
    ListView mLvGoods;
    @BindView(R.id.tv_goods)
    TextView mTvGoods;
    private int mIntType;
    private List<Stock> mListShow = new ArrayList<>();
    private List<Stock> mListAll = new ArrayList<>();
    private List<String> mListBaseketSaved = new ArrayList<>();
    private GoodsAdapter mGoodsAdapter;
    private Stock mStock;
    private boolean loadWareHouse;
    private boolean loadCategory;
    private boolean loadList;
    private ScanEditText mEtBasketForAdd;
    private ArrayList<String> mListBasketNum;
    private BasketAdapter mBasketAdapter;
    private ListView mLvBasket;
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
    private DBManager mDBManager;
    private SimilarPopWindow mSimilarPopWindow;
    private ListView mLvBasketSaved;
    private BasketAdapter mBasketSavedAdapter;
    private Button mBtnStockOut;
    private Button mBtnAllocate;
    private Button mBtnInventory;
    private VideoFragment mVideoFragment;

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
        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        getSupportFragmentManager().beginTransaction().add(R.id.container, mVideoFragment).commitAllowingStateLoss();
        //库存数
        View llStockNum = findViewById(R.id.ll_stock_num);
        TextView tvTitleStockNum = llStockNum.findViewById(R.id.tv_title);
        mTvTitleStockNumUnit = llStockNum.findViewById(R.id.tv_unit);
        tvTitleStockNum.setText("库存数");
        mEtStockNum = (SearchAndFocusEditText) llStockNum.findViewById(R.id.ed_content);
        mEtStockNum.setEnabled(false);
        mEtStockNum.setBackground(null);
        mEtStockNum.setTextColor(getResources().getColor(R.color.black));
        mEtStockNum.setTextSize(getResources().getDimension(R.dimen._16sp));
        View llCurrentWeight = findViewById(R.id.ll_current_weight);
        TextView tvTitleCurrentWeight = llCurrentWeight.findViewById(R.id.tv_title);
        mEtCurrentWeight = (SearchAndFocusEditText) llCurrentWeight.findViewById(R.id.ed_content);
        mEtCurrentWeight.setEnabled(false);
        mEtCurrentWeight.setBackground(null);
        mEtCurrentWeight.setTextColor(getResources().getColor(R.color.red));
        mEtCurrentWeight.setTextSize(getResources().getDimension(R.dimen._16sp));
        tvTitleCurrentWeight.setText("当前重量");
        View llLeather = findViewById(R.id.ll_buckles_leather);
        TextView tvBucklesLeather = llLeather.findViewById(R.id.tv_title);
        mEtLeather = (SearchAndFocusEditText) llLeather.findViewById(R.id.ed_content);
        tvBucklesLeather.setText("扣皮");
        View llDeduct = findViewById(R.id.ll_deduct_weight);
        TextView tvTitleDeductWeight = llDeduct.findViewById(R.id.tv_title);
        mEtDeduct = (SearchAndFocusEditText) llDeduct.findViewById(R.id.ed_content);
        tvTitleDeductWeight.setText("扣重");
        View llCount = findViewById(R.id.ll_count);
        TextView tvCount = llCount.findViewById(R.id.tv_title);
        mTvUnitCount = llCount.findViewById(R.id.tv_unit);
        mEtCount = (EditText) llCount.findViewById(R.id.ed_content);
        mLvBasketSaved = (ListView) findViewById(R.id.lv_basket_saved);

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
                //初始化详情
                initPopInfo();

                setInfo();
            }
        });

        mSpGoodsCategory.setCustomItemSelectedListener(this);
        mSpWareHouse.setCustomItemSelectedListener(this);
//        mEtKeySearch.setOnInputFinishListener(new onInputFinishListener() {
//            @Override
//            public void onFinish(String key) {
//                filterList();
//            }
//        });
        mEtBasket.setFilters(EtMaxLengthUtil.getFilter());
        mEtBasket.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key) && key.length() == Common.BasketLength) {
                    showLoadingDialog(false);
                    CommPresenter.queryStock(getActivity(), 0, 0, 0, key);
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
    }

    @Override
    protected void loadDate() {
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
        SimilarPresenter.getDropdownLeafCategory(this);
        CommPresenter.queryStock(this, 0, 0, 0, "");
    }

    @OnClick({R.id.tv_pop_info, R.id.iv_similar_search})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.tv_pop_info:
                if (mSimilarPopWindow != null && mStock != null) {
                    mSimilarPopWindow.show();
                } else {
                    ToastUtil.showShortToast(getContext(), "请选择商品");
                }
                break;
            case R.id.iv_similar_search:
                showLoadingDialog(false);
                filterList();
                break;
            default:
                break;
        }
    }

    /**
     * 初始化详情
     */
    private void initPopInfo() {
        if (mSimilarPopWindow == null) {
            mSimilarPopWindow = new SimilarPopWindow(getContext(), mTvGoods);
        }
        mSimilarPopWindow.setInfo(mStock);
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

    private void shotResult(String path) {
        StockOutRecord record = BeanSimilar.createStoreOutRecord(getContext(),
                mStock,
                mSpTypeStockOut,
                mEtCount);
        DbImageUpload dbImageUpload = new DbImageUpload();
        dbImageUpload.setImagePath(path);
        dbImageUpload.setDate(DateUtils.StringData());
        dbImageUpload.setLine(GsonUtil.getGson().toJson(record));
        dbImageUpload.setType(Common.DbType.TYPE_STORE_OUT);
        getDbManager().insertDbImageUpload(dbImageUpload);
        showSuccessResult("出库");
        setBtnEnable(mBtnStockOut);
        dismissLoadingDialog();
    }

    /**
     * 初始化出库布局
     */
    private void initStockOutParams() {
        View layout = LayoutInflater.from(this).inflate(R.layout.layout_type_stockout, null);
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
        mEtBasketForAdd = ((ScanEditText) layout.findViewById(R.id.et_backet));
        InputFilter[] filters = {new InputFilter.LengthFilter(6)};
        mEtBasketForAdd.setFilters(filters);
        mLvBasket = ((ListView) layout.findViewById(R.id.lv_basket));
        mEtBasketForAdd.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                addBasketNum(key);
            }
        });
        mBtnAllocate = layout.findViewById(R.id.btn_allocate);
        mBtnAllocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog(false);
                Warehouse warehouse = mSpWareHouse.getSelectedItem();
                Warehouse wareHouseOut = mSpWareHouseIn.getSelectedItem();
                if (warehouse == null || wareHouseOut == null
                        || TextUtils.isEmpty(warehouse.getName()) || TextUtils.isEmpty(wareHouseOut.getName())) {
                    dismissLoadingDialog();
                    return;
                }
                if (warehouse.getName().equals(wareHouseOut.getName())
                        && warehouse.getCode().equals(wareHouseOut.getCode())) {
                    ToastUtil.showShortToast(getContext(), "调入仓库不能选择所在仓库");
                    dismissLoadingDialog();
                    return;
                }
                if (checkToRequest("调拨数量不能为空")) {
                    mBtnAllocate.setEnabled(false);
                    AllocateRecord allocateRecord = BeanSimilar.createAllocateRecord(getContext(),
                            mStock,
                            mSpWareHouseIn,
                            mEtCount, mListBasketNum);
                    DbImageUpload dbImageUpload = new DbImageUpload();
                    dbImageUpload.setDate(DateUtils.StringData());
                    dbImageUpload.setLine(GsonUtil.getGson().toJson(allocateRecord));
                    dbImageUpload.setType(Common.DbType.TYPE_ALLOCATE);
                    getDbManager().insertDbImageUpload(dbImageUpload);
                    showSuccessResult("调拨");
                    setBtnEnable(mBtnAllocate);
                }
                dismissLoadingDialog();
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
                showLoadingDialog(false);
                if (checkToRequest("实际数量不能为空")) {
                    mBtnInventory.setEnabled(false);
                    InventoryRecord inventoryRecord = BeanSimilar.createInventoryRecord(getContext(),
                            mStock,
                            mEtCount);
                    DbImageUpload dbImageUpload = new DbImageUpload();
                    dbImageUpload.setDate(DateUtils.StringData());
                    dbImageUpload.setLine(GsonUtil.getGson().toJson(inventoryRecord));
                    dbImageUpload.setType(Common.DbType.TYPE_CHECK);
                    getDbManager().insertDbImageUpload(dbImageUpload);
                    showSuccessResult("盘点");
                    setBtnEnable(mBtnInventory);
                }
                dismissLoadingDialog();
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

    @Override
    public void onGetDropdownLeafCategorySuccess(List<GoodsCategory> result) {
        GoodsCategory category = new GoodsCategory();
        category.setName("全部");
        result.add(0, category);
        mSpGoodsCategory.setList(result);
        loadCategory = true;
        filterList();
    }

    @Override
    public void onGetDropdownLeafCategoryFailed(int errorType, String failString) {
        ToastUtil.showShortToast(getContext(), failString);
        dismissLoadingDialog();
    }

    @Override
    public void onQueryStockSuccess(PageData<Stock> result) {
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
            mEtBasket.setText("");
            clearContent(values.get(0));
            setInfo();
            dismissLoadingDialog();
        } else {
            mListAll.clear();
            mListAll.addAll(values);
            loadList = true;
            filterList();
        }
    }

    @Override
    public void onQueryStockFailed(int errorType, String failString) {
        ToastUtil.showShortToast(getContext(), failString);
        dismissLoadingDialog();
    }

    private DBManager getDbManager() {
        if (mDBManager == null) {
            mDBManager = DBManager.getInstance(this.getApplicationContext());
        }
        return mDBManager;
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
        //清空详情
        if (mSimilarPopWindow != null) {
            mSimilarPopWindow.setInfo(null);
        }
        if (mBasketSavedAdapter != null) {
            mListBaseketSaved.clear();
            mBasketSavedAdapter.notifyDataSetChanged();
        }
        ToastUtil.showShortToast(getContext(), typeStr + "成功");
        mEtBasket.requestFocus();
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

        mListBaseketSaved.clear();
        List<String> basketCodes = mStock.getBasketCodes();
        if (basketCodes != null && basketCodes.size() > 0) {
            if (mBasketSavedAdapter == null) {
                mBasketSavedAdapter = new BasketAdapter(getContext(), mListBaseketSaved);
            }
            mListBaseketSaved.addAll(basketCodes);
        }
        if (mBasketSavedAdapter != null) {
            mBasketSavedAdapter.notifyDataSetChanged();
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
                mEtBasketForAdd.setText("");
                if (mListBasketNum != null && mListBasketNum.size() > 0) {
                    mListBasketNum.clear();
                    mBasketAdapter.notifyDataSetChanged();
                }
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
     * 添加周转筐
     */
    private void addBasketNum(String basketNum) {
        if (TextUtils.isEmpty(basketNum) || basketNum.length() != Common.BasketLength) {
            return;
        }
        if (mListBasketNum == null) {
            mListBasketNum = new ArrayList<>();
        } else {
            //判重
            for (String basket : mListBasketNum) {
                if (basket.equals(basketNum)) {
                    ToastUtil.showShortToast(getContext(), "周转筐已添加");
                    mEtBasketForAdd.setText("");
                    return;
                }
            }
        }
        mListBasketNum.add(0, basketNum);
        if (mBasketAdapter == null) {
            mBasketAdapter = new BasketAdapter(this, mListBasketNum);
            mBasketAdapter.setOnBasketRemoveListener(new BasketAdapter.OnBasketRemoveListener() {
                @Override
                public void onRemove(String basketNum, int position) {
                    mListBasketNum.remove(position);
                    mBasketAdapter.notifyDataSetChanged();
                }
            });
            mLvBasket.setAdapter(mBasketAdapter);
        } else {
            mBasketAdapter.notifyDataSetChanged();
        }
        mEtBasketForAdd.setText("");
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
}
