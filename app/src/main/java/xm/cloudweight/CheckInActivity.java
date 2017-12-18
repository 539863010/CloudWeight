package xm.cloudweight;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.purchase.PurchaseBill;
import com.xmzynt.storm.service.purchase.PurchaseBillLine;
import com.xmzynt.storm.service.purchase.PurchaseData;
import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.service.wms.stockin.StockInType;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.PageData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.BeanPrinter;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.impl.CheckInImpl;
import xm.cloudweight.presenter.CheckInPresenter;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.KeyBoardUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.EtMaxLengthUtil;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.PrinterUtil;
import xm.cloudweight.utils.dao.DBManager;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CheckInInfoPopWindow;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.PrinterView;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.SearchEditText;
import xm.cloudweight.widget.adapter.BasketAdapter;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;

/**
 * @author wyh
 * @description: 验收
 * @create 2017/10/30
 */
public class CheckInActivity extends BaseActivity implements
        CheckInImpl.OnQueryPurchaseBillListener
        , CheckInImpl.OnGetPurchaseBillListener
        , CheckInImpl.OnScanToPurchaseDataListener
        , Spinner.OnItemSelectedListener, VideoFragment.OnInstrumentListener {

    @BindView(R.id.sp_ware_house)
    DataSpinner<Warehouse> mSpWareHouse;
    @BindView(R.id.sp_purchaseBill)
    DataSpinner<PurchaseBill> mSpPurchaseBill;
    @BindView(R.id.lv_line)
    ListView mLvPurchase;
    @BindView(R.id.lv_basket)
    ListView mLvBasket;
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
    @BindView(R.id.et_basket)
    ScanEditText mEtBasket;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.btn_stock_in)
    Button mBtnStockIn;
    @BindView(R.id.btn_stock_cross)
    Button mBtnStockCross;
    private List<PurchaseBillLine> mListPurchaseBillLineShow = new ArrayList<>();
    private List<PurchaseBillLine> mListPurchaseBillLineAll = new ArrayList<>();
    private List<String> mListBasketNum;
    private PurchaseBillLineAdapter mAdapterPurchase;
    private BasketAdapter mAdapterBasket;
    private PurchaseBillLine mPurchaseBillLine;
    private EditText mEtWeightCurrent;
    private SearchAndFocusEditText mEtWeightAccumulate;
    private SearchAndFocusEditText mEtBucklesLeather;
    private SearchAndFocusEditText mEtDeductWeight;
    private EditText mEtNumWarehousing;
    private SearchEditText mEtWarehousingUnitPrice;
    private SearchEditText mEtWarehousingAmount;
    private TextView mTvNumWarehousingUnit;
    private BigDecimal mCoefficientStandard;
    private PurchaseBill mPurchaseBill;
    private PurchaseData mPurchaseData;
    /**
     * 记录累计重量(已入库数 的 多次累计)
     */
    private Map<String, String> mMapAccumulate = new HashMap<>();
    private TextView mTvWeightAccumulateUnit;
    private static final int TYPE_STOREIN = 1;
    private static final int TYPE_CROSS = 2;
    private int mIntTypeUpload;
    private DBManager mDBManager;
    private ImageView mIvSub;
    private ImageView mIvAdd;
    private CheckInInfoPopWindow mCheckInInfoPopWindow;
    private PrinterView mPrinterView;
    private VideoFragment mVideoFragment;

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
        mEtWarehousingUnitPrice = (SearchEditText) getEditText(R.id.ll_warehousing_unit_price, "入库单价");
        mEtWarehousingUnitPrice.setEnabled(false);
        ((TextView) findViewById(R.id.ll_warehousing_unit_price).findViewById(R.id.tv_unit)).setText("元");
        mEtWarehousingAmount = (SearchEditText) getEditText(R.id.ll_warehousing_amount, "入库金额");
        ((TextView) findViewById(R.id.ll_warehousing_amount).findViewById(R.id.tv_unit)).setText("元");

        mAdapterPurchase = new PurchaseBillLineAdapter(this, mListPurchaseBillLineShow);
        mLvPurchase.setAdapter(mAdapterPurchase);

        setViewListener();
    }

    private void setViewListener() {
//        mEtNumWarehousing.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mEtNumWarehousing.setText("5.00");
//                mEtNumWarehousing.postDelayed(this, 100);
//            }
//        },100);
        mSpWareHouse.setCustomItemSelectedListener(this);
        mSpPurchaseBill.setCustomItemSelectedListener(this);
        mEtBasket.setFilters(EtMaxLengthUtil.getFilter());
        mEtBasket.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key) && key.length() == Common.BasketLength) {
                    addBasketNum(key);
                }
            }
        });
        mLvPurchase.setOnItemClickListener(mOnItemPurchaseClickListener);
        mEtPurChaseLabel.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key)) {
                    showLoadingDialog(false);
                    //请求扫描接口
                    CheckInPresenter.scanToPurchaseData(getActivity(), key);
                    if (mEtBasket != null) {
                        mEtBasket.requestFocus();
                    }
                }
            }
        });
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
                if (mPurchaseBillLine != null
                        && mPurchaseBillLine.getWeightCoefficient() != null
                        && mPurchaseBillLine.getWeightCoefficient().doubleValue() != 0) {
                    weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
                }
                BigDecimal finallyNum = currentWeight.subtract(leather).subtract(deduct).divide(weightCoefficient, RoundingMode.HALF_EVEN);
                mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(finallyNum));
            }
        });
//        mEtKeySearch.setOnInputFinishListener(new onInputFinishListener() {
//            @Override
//            public void onFinish(String key) {
//                keySearch(key);
//            }
//        });
        mEtNumWarehousing.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString();
                if (mPurchaseBillLine == null) {
                    return;
                }
                if (!TextUtils.isEmpty(key)) {
                    BigDecimal numWarehousing = new BigDecimal(key);
                    BigDecimal unitPrice = getEtBigDecimal(mEtWarehousingUnitPrice);
                    mEtWarehousingAmount.setText(BigDecimalUtil.toScaleStr(numWarehousing.multiply(unitPrice)));
                } else {
                    mEtWarehousingAmount.setText("0.00");
                }
            }
        });
        mEtWarehousingUnitPrice.setOnInputFinishListener(new onInputFinishListener() {
            private String mPrePrice;

            @Override
            public void onFinish(String key) {
                if (mPurchaseBillLine == null) {
                    return;
                }
                if (!TextUtils.isEmpty(key) && !TextUtils.equals(key, mPrePrice) && isFocusOn()) {
                    mPrePrice = key;
                    BigDecimal unitPrice = new BigDecimal(key);
                    BigDecimal numWarehousing = getEtBigDecimal(mEtNumWarehousing);
                    mEtWarehousingAmount.setText(BigDecimalUtil.toScaleStr(numWarehousing.multiply(unitPrice)));
                }
            }
        });
        mEtWarehousingAmount.setOnInputFinishListener(new onInputFinishListener() {
            private String mPreAmount;

            @Override
            public void onFinish(String key) {
                if (mPurchaseBillLine == null) {
                    return;
                }
                if (!TextUtils.isEmpty(key) && !TextUtils.equals(key, mPreAmount) && isFocusOn()) {
                    mPreAmount = key;
                    BigDecimal amount = new BigDecimal(key);
                    BigDecimal numWarehousing = getEtBigDecimal(mEtNumWarehousing);
                    if (numWarehousing.doubleValue() > 0) {
                        mEtWarehousingUnitPrice.setText(BigDecimalUtil.toScaleStr(amount.divide(numWarehousing, RoundingMode.HALF_EVEN)));
                    }
                }
            }
        });
        mIvSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPurchaseBillLine != null && mPurchaseBillLine.getWeightCoefficient() == null) {
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
                if (mPurchaseBillLine != null && mPurchaseBillLine.getWeightCoefficient() == null) {
                    BigDecimal count = getEtBigDecimal(mEtNumWarehousing);
                    mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(count.add(new BigDecimal(1))));
                }
            }
        });
        mEtWeightAccumulate.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
        mEtBucklesLeather.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
        mEtDeductWeight.setOnInputFinishListener(mOnInputFinishListenerSetStoreInNum);
    }

    /**
     * 改变单价文本框和金额文本框才改变单价
     **/
    private boolean isFocusOn() {
        return (mEtWarehousingAmount.isFocusable() || mEtWarehousingUnitPrice.isFocusable());
    }

    /**
     * 文本框监听
     */
    private onInputFinishListener mOnInputFinishListenerSetStoreInNum = new onInputFinishListener() {
        @Override
        public void onFinish(String key) {
            if (mPurchaseBillLine == null) {
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

        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(this);
        if (listWareHouse != null) {
            mSpWareHouse.setList(listWareHouse);
            showLoadingDialog(true);
            queryList();
        } else {
            ToastUtil.showShortToast(this, "未获取到仓库列表信息");
        }
    }

    @Override
    public String getBaseTitle() {
        return "验收";
    }

    /**
     * 查找关键字
     */
    private void keySearch(String key) {
        if (!TextUtils.isEmpty(key)) {
            List<PurchaseBillLine> listSearch = new ArrayList<>();
            if (mListPurchaseBillLineShow.size() > 0) {
                for (PurchaseBillLine line : mListPurchaseBillLineShow) {
                    String name = line.getGoods().getName();
                    if (!TextUtils.isEmpty(name) && name.contains(key)) {
                        listSearch.add(line);
                    }
                }
            }
            mListPurchaseBillLineShow.clear();
            mListPurchaseBillLineShow.addAll(listSearch);
        } else {
            mListPurchaseBillLineShow.clear();
            mListPurchaseBillLineShow.addAll(mListPurchaseBillLineAll);
        }
        mAdapterPurchase.notifyDataSetChanged();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.sp_ware_house:
                showLoadingDialog(false);
                queryList();
                break;
            case R.id.sp_purchaseBill:
                //单独获取
                showLoadingDialog(false);
                PurchaseBill purchaseBill = mSpPurchaseBill.getSelectedItem();
                CheckInPresenter.getPurchaseBill(getActivity(), purchaseBill.getUuid());
                break;
            default:
                break;
        }
    }

    /**
     * 刷新商品列表数据
     */
    private void setPurchaseBillLine(PurchaseBill purchaseBill) {
        mPurchaseBill = purchaseBill;
        mPurchaseData = null;
        mListPurchaseBillLineAll.clear();
        if (purchaseBill != null) {
            mListPurchaseBillLineAll.addAll(purchaseBill.getLines());
        }
        mListPurchaseBillLineShow.clear();
        mListPurchaseBillLineShow.addAll(mListPurchaseBillLineAll);
        mAdapterPurchase.notifyDataSetChanged();
        dismissLoadingDialog();
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
                    mEtBasket.setText("");
                    return;
                }
            }
        }
        mListBasketNum.add(0, basketNum);
        if (mAdapterBasket == null) {
            mAdapterBasket = new BasketAdapter(this, mListBasketNum);
            mAdapterBasket.setOnBasketRemoveListener(new BasketAdapter.OnBasketRemoveListener() {
                @Override
                public void onRemove(String basketNum, int position) {
                    mListBasketNum.remove(position);
                    mAdapterBasket.notifyDataSetChanged();
                }
            });
            mLvBasket.setAdapter(mAdapterBasket);
        } else {
            mAdapterBasket.notifyDataSetChanged();
        }
        mEtBasket.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * 查找列表
     */
    private void queryList() {
        String selectedDate = mBtnDate.getText().toString().trim();
        Warehouse selectedWareHouse = mSpWareHouse.getSelectedItem();
        if (selectedWareHouse != null) {
            clearContent();
            //清除商品列表信息
            clearGoodsList();
            CheckInPresenter.queryPurchaseBill(this, 0, 0, 0, selectedDate, selectedWareHouse.getUuid());
        } else {
            dismissLoadingDialog();
//            ToastUtil.showShortToast(getContext(), "请选择仓库");
        }
    }

    @OnClick({R.id.btn_stock_in, R.id.btn_stock_cross, R.id.btn_date, R.id.tv_pop_info, R.id.iv_sort_out_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_date:
                //显示时间选择框
                String dateStr = mBtnDate.getText().toString().trim();
                DatePickerDialogUtil.show(getContext(), dateStr, mOnDateSetListener);
                break;
            case R.id.btn_stock_in:
                if (TextUtils.isEmpty(mEtNumWarehousing.getText().toString().trim())) {
                    ToastUtil.showShortToast(getContext(), "入库数量不能为空");
                    return;
                }
                mIntTypeUpload = TYPE_STOREIN;
                mBtnStockIn.setEnabled(false);
                if (mVideoFragment.isLight()) {
                    mVideoFragment.screenshot(mHandlerShotPic);
                } else {
                    showLoadingDialog(false);
                    shotResult(null);
                }
                break;
            case R.id.btn_stock_cross:
                if (TextUtils.isEmpty(mEtNumWarehousing.getText().toString().trim())) {
                    ToastUtil.showShortToast(getContext(), "入库数量不能为空");
                    return;
                }
                if (mPurchaseBillLine != null) {
                    if (!TextUtils.isEmpty(mPurchaseBillLine.getSourcePlanUuid())
                            && !TextUtils.isEmpty(mPurchaseBillLine.getGoodsOrderBillNumber())) {
                        mIntTypeUpload = TYPE_CROSS;
                        mBtnStockCross.setEnabled(false);
                        if (mVideoFragment.isLight()) {
                            mVideoFragment.screenshot(mHandlerShotPic);
                        } else {
                            showLoadingDialog(false);
                            shotResult(null);
                        }
                    } else {
                        ToastUtil.showShortToast(getContext(), "无客户信息");
                    }
                }
                break;
            case R.id.tv_pop_info:
                if (null != mCheckInInfoPopWindow && mPurchaseBillLine != null) {
                    mCheckInInfoPopWindow.show();
                } else {
                    ToastUtil.showShortToast(getContext(), "请选择商品");
                }
                break;
            case R.id.iv_sort_out_search:
                String key = mEtKeySearch.getText().toString().trim();
                keySearch(key);
                break;
            default:
                break;
        }
    }

    /**
     * 详情
     */
    private void initInfoPop() {
        if (null == mCheckInInfoPopWindow) {
            mCheckInInfoPopWindow = new CheckInInfoPopWindow(getContext(), mTvInfoPurchaseRemark);
        }
        mCheckInInfoPopWindow.setInfo(mPurchaseBillLine);
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
            queryList();
        }
    };

    /**
     * 商品列表item点击
     */
    private AdapterView.OnItemClickListener mOnItemPurchaseClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mPurchaseBillLine = mListPurchaseBillLineShow.get(i);
            //初始化详情
            initInfoPop();
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
        // 入库单价   入库单价=采购单价
        setEditText(mEtWarehousingUnitPrice, BigDecimalUtil.toScaleStr(mPurchaseBillLine.getPrice()));
        // 入库金额   入库金额=入库数量*入库单价   入库金额可以修改，修改后重新计算入库单价
        setEditText(mEtWarehousingAmount, BigDecimalUtil.toScaleStr(mPurchaseBillLine.getSubtotal()));
    }

    /**
     * 清空商品列表
     */
    private void clearGoodsList() {
        if (mListPurchaseBillLineShow.size() > 0) {
            mListPurchaseBillLineShow.clear();
        }
        mAdapterPurchase.notifyDataSetChanged();
    }

    /**
     * 清除商品信息
     */
    private void clearText() {
        if (mAdapterBasket != null && mListBasketNum.size() > 0) {
            mListBasketNum.clear();
            mAdapterBasket.notifyDataSetChanged();
        }

        //清空数据
        mEtBasket.setText("");
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
        mEtWarehousingUnitPrice.setText("");
        mEtWarehousingAmount.setText("");

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
                    setBtnEnable();
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
        StockInRecord record = prepareConfig();
        if (mIntTypeUpload == TYPE_STOREIN) {
            //入库
            DbImageUpload db = new DbImageUpload();
            db.setDate(DateUtils.StringData());
            db.setImagePath(path);
            db.setLine(GsonUtil.getGson().toJson(record));
            db.setType(Common.DbType.TYPE_ChECK_IN_STORE_IN);
            getDbManager().insertDbImageUpload(db);
            ToastUtil.showShortToast(getContext(), "入库成功");
        } else {
            //越库
            DbImageUpload db = new DbImageUpload();
            db.setDate(DateUtils.StringData());
            db.setImagePath(path);
            db.setLine(GsonUtil.getGson().toJson(record));
            db.setType(Common.DbType.TYPE_ChECK_IN_CROSS_OUT);
            getDbManager().insertDbImageUpload(db);
            ToastUtil.showShortToast(getContext(), "越库成功");
        }
        refreshSuccessData();
        setBtnEnable();
        dismissLoadingDialog();
    }

    private void setBtnEnable() {
        if (!mBtnStockIn.isEnabled()) {
            mBtnStockIn.setEnabled(true);
        }
        if (!mBtnStockCross.isEnabled()) {
            mBtnStockCross.setEnabled(true);
        }
    }

    /**
     * 创建入库、越库 请求体
     */
    private StockInRecord prepareConfig() {
        Warehouse warehouse = mSpWareHouse.getSelectedItem();
        if (warehouse == null) {
            return null;
        }
        if (mPurchaseBillLine != null && (mPurchaseBill != null || mPurchaseData != null)) {
            StockInRecord sir = new StockInRecord();
            sir.setWarehouse(new UCN(warehouse.getUuid(), warehouse.getCode(), warehouse.getName()));
            sir.setGoods(mPurchaseBillLine.getGoods());
            //规格
            sir.setGoodsSpec(mPurchaseBillLine.getGoodsSpec());
            sir.setGoodsUnit(mPurchaseBillLine.getGoodsUnit());
            //操作人名字
            Merchant merchant = LocalSpUtil.getMerchant(this);
            sir.setOperatorName(merchant != null ? merchant.getName() : "");
            //备注
            sir.setRemark(mPurchaseBillLine.getRemark());
            //产地
            sir.setOrigin(mPurchaseBillLine.getOrigin());
            //来源采购明细uuid
            sir.setSourceBillLineUuid(mPurchaseBillLine.getUuid());
            //扣重数
            BigDecimal deductQty = getEtBigDecimal(mEtDeductWeight);
            sir.setDeductQty(deductQty);
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
            //入库单价
            sir.setPrice(getEtBigDecimal(mEtWarehousingUnitPrice));
            sir.setStockInType(StockInType.purchaseIn);
            //周转筐列表
            sir.setBasketCodes(mListBasketNum);
            if (mPurchaseBill != null && mPurchaseData == null) {
                //通过查询订单
                sir.setOrg(mPurchaseBill.getOrg());
                sir.setSupplier(mPurchaseBill.getSupplier());
                //来源采购单号
                sir.setSourceBillNumber(mPurchaseBill.getBillNumber());
                //交货日期
                sir.setDeliveryTime(mPurchaseBill.getDeliveryTime());
            } else {
                //通过扫描
                sir.setSupplier(mPurchaseData.getSupplier());
                Date deliveryTime = DateUtils.converToDate(DateUtils.getTime2(mPurchaseData.getDeliveryTime()));
                sir.setDeliveryTime(deliveryTime);
                sir.setSourceBillNumber(mPurchaseData.getPurchaseBillNumber());
            }
            //标准系数
            sir.setCoefficient(mCoefficientStandard);
            if (mCheckInInfoPopWindow != null) {
                //批号
                sir.setBatchNumber(mCheckInInfoPopWindow.getBatchNum());
                //上市批号
                sir.setListingCertificateNo(mCheckInInfoPopWindow.getCredentialsNum());
                //有效日期
                Date effectiveDate = DateUtils.converToDate(mCheckInInfoPopWindow.getEffectiveDate());
                sir.setEffectiveDate(effectiveDate);
                //生产日期
                Date produceDate = DateUtils.converToDate(mCheckInInfoPopWindow.getProduceDate());
                sir.setProduceDate(produceDate);
                //备注
                sir.setRemark(mCheckInInfoPopWindow.getRemark());
                //产地
                sir.setOrigin(mCheckInInfoPopWindow.getProduceArea());
            }
            //入库时间
            sir.setInDate(new Date());
            //设置机构
            if (merchant != null) {
//                如果没返回org  就是当前登陆的人是公司帐号 就用此帐号的userUuid和name  作为org的IdName
                IdName org = merchant.getOrg();
                if (org == null) {
                    sir.setOrg(new IdName(merchant.getUuid(), merchant.getName()));
                } else {
                    sir.setOrg(org);
                }
            }
            showLog(GsonUtil.getGson().toJson(sir));
            return sir;
        }
        return null;
    }

    @Override
    public void onQueryPurchaseBillSuccess(PageData<PurchaseBill> data) {
        List<PurchaseBill> values = data.getValues();
        mSpPurchaseBill.setList(values);
        if (values != null && values.size() > 0) {
            PurchaseBill purchaseBill = values.get(0);
            CheckInPresenter.getPurchaseBill(getActivity(), purchaseBill.getUuid());
        } else {
            setPurchaseBillLine(null);
        }
    }

    private void clearContent() {
        //清除关键字
        mEtKeySearch.setText("");
        //清除文本
        clearText();
    }

    @Override
    public void onQueryPurchaseBillFailed(String message) {
        //清空list跟供应商下拉数据
        mSpPurchaseBill.setList(new ArrayList<PurchaseBill>());
        mListPurchaseBillLineShow.clear();
        mAdapterPurchase.notifyDataSetChanged();
        ToastUtil.showShortToast(getContext(), message);
        dismissLoadingDialog();
    }

    @Override
    public void onGetPurchaseBillSuccess(PurchaseBill purchaseBill) {
        setPurchaseBillLine(purchaseBill);
    }

    @Override
    public void onGetPurchaseBillFailed(String message) {
        ToastUtil.showShortToast(getContext(), message);
        dismissLoadingDialog();
    }

    @Override
    public void onScanToPurchaseDataSuccess(PurchaseData purchaseData) {
        KeyBoardUtils.closeKeybord(mEtPurChaseLabel, getContext());
        mPurchaseData = purchaseData;
        mPurchaseBill = null;
        mCoefficientStandard = purchaseData.getCoefficient();
        mPurchaseBillLine = purchaseData.getPurchaseBillLine();
        mEtPurChaseLabel.setText("");
        setPurchaseBillLineInfo();
        dismissLoadingDialog();
    }

    @Override
    public void onScanToPurchaseDataFailed(int errorType, String message) {
        mEtPurChaseLabel.setText("");
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
     */
    private void refreshSuccessData() {
        // 保存累计重量
        if (mPurchaseBillLine != null) {
            BigDecimal accumulate = getEtBigDecimal(mEtWeightAccumulate);
            //转化为kg
            BigDecimal weightCoefficient = mPurchaseBillLine.getWeightCoefficient();
            BigDecimal numWarehousing = getEtBigDecimal(mEtNumWarehousing);
            //打印越库标签
            if (mIntTypeUpload == TYPE_CROSS) {
                // 打印标签
                printer(weightCoefficient, numWarehousing);
            }
            if (weightCoefficient != null) {
                mMapAccumulate.put(mPurchaseBillLine.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing).multiply(weightCoefficient)));
            } else {
                mMapAccumulate.put(mPurchaseBillLine.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing)));
            }
            //保存已入库数
            mPurchaseBillLine.setHasStockInQty(mPurchaseBillLine.getHasStockInQty().add(getEtBigDecimal(mEtNumWarehousing)));
            clearContent();
            mPurchaseBillLine = null;
            mCoefficientStandard = null;
            mAdapterPurchase.notifyDataSetChanged();
            //光标移动到采购标签
            mEtPurChaseLabel.requestFocus();
            //清空详情数据
            if (mCheckInInfoPopWindow != null) {
                mCheckInInfoPopWindow.setInfo(null);
            }
        }
    }

    private void printer(BigDecimal weightCoefficient, BigDecimal numWarehousing) {
        if (mPrinterView == null) {
            mPrinterView = (PrinterView) findViewById(R.id.pv);
        }
        BeanPrinter beanPrinter = new BeanPrinter();
        beanPrinter.setGoodsName(mPurchaseBillLine.getGoods().getName());
        if (weightCoefficient != null && weightCoefficient.doubleValue() != 0) {
            beanPrinter.setCount(BigDecimalUtil.toScaleStr(numWarehousing.multiply(weightCoefficient)).concat("kg"));
        } else {
            beanPrinter.setCount(BigDecimalUtil.toScaleStr(numWarehousing).concat(mPurchaseBillLine.getGoodsUnit().getName()));
        }
        IdName customer = mPurchaseBillLine.getCustomer();
        beanPrinter.setCustomer(customer != null && !TextUtils.isEmpty(customer.getName()) ? customer.getName() : null);
        IdName customerDept = mPurchaseBillLine.getCustomerDept();
        beanPrinter.setDepartment(customerDept != null && !TextUtils.isEmpty(customerDept.getName()) ? customerDept.getName() : null);
        beanPrinter.setCode(Common.getPlatformTraceCode());
        mPrinterView.set(beanPrinter);
        String filePath = mPrinterView.getPath();
        PrinterUtil.printer(this, filePath);
    }

    /**
     * 采购订单Adapter
     **/
    private class PurchaseBillLineAdapter extends CommonAdapter4Lv<PurchaseBillLine> {

        private PurchaseBillLineAdapter(Context context, List<PurchaseBillLine> data) {
            super(context, R.layout.item_line, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, PurchaseBillLine line, int position) {
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
            if (mPurchaseBillLine != null && mPurchaseBillLine.getWeightCoefficient() != null) {
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
