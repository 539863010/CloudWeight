package xm.cloudweight;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.tencent.bugly.crashreport.CrashReport;
import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.user.customer.CustomerLevel;
import com.xmzynt.storm.service.wms.stock.Stock;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;
import com.xmzynt.storm.util.query.PageData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.bean.CustomSortOutData;
import xm.cloudweight.camera.instrument.Instrument;
import xm.cloudweight.comm.Common;
import xm.cloudweight.fragment.InputFragment;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.impl.CommImpl;
import xm.cloudweight.service.RequestDataService;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.IsBottomUtil;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.BuglyUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.FilterUtil;
import xm.cloudweight.utils.bussiness.GetImageFile;
import xm.cloudweight.utils.bussiness.ListComparator;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.MessageUtil;
import xm.cloudweight.utils.bussiness.printer.PrinterBean;
import xm.cloudweight.utils.bussiness.printer.PrinterSortOut;
import xm.cloudweight.utils.dao.DbRefreshUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.utils.dao.bean.DbRequestData;
import xm.cloudweight.utils.onNoDoubleClickListener;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.HistorySortOutPopWindow;
import xm.cloudweight.widget.ScalableTextView;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.OnDeleteListener;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

/**
 * @author wyh
 * @Description: 分拣
 * @create 2017/10/29
 */
public class SortOutActivity extends BaseActivity implements
        CommImpl.OnQueryStockListener
        , AdapterView.OnItemSelectedListener
        , OnDeleteListener,
        onInputFinishListener, VideoFragment.OnInstrumentListener,
        AbsListView.OnScrollListener,
        PopupWindow.OnDismissListener {

    public static final int TYPE_WEIGHT = 0;
    public static final int TYPE_COUNT = 1;
    public static final int UPLOAD_PAGE_SIZE = 50;

    @BindView(R.id.btn_sort_out_history)
    Button mBtnSortOutHistory;
    @BindView(R.id.sp_ware_house)
    DataSpinner<Warehouse> mSpWareHouse;
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
    @BindView(R.id.et_leather_sort_out)
    EditText mEtLeather;
    @BindView(R.id.et_weight)
    EditText mEtWeight;
    @BindView(R.id.et_tag)
    ScanEditText mEtTag;
    @BindView(R.id.et_goods_name_or_id)
    SearchAndFocusEditText mEtGoodsNameOrId;
    @BindView(R.id.et_customers)
    SearchAndFocusEditText mEtCustomers;
    @BindView(R.id.et_custom_group)
    SearchAndFocusEditText mEtCustomGroup;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.btn_sort_out)
    Button mBtnSortOut;
    @BindView(R.id.btn_request)
    Button mBtnRequest;
    @BindView(R.id.iv_delete)
    ImageView mIvDelete;
    @BindView(R.id.ll_leather)
    View mLlLeather;
    @BindView(R.id.ll_weight)
    View mLlWeight;
    private List<CustomSortOutData> mListShow = new ArrayList<>();
    private ArrayList<CustomSortOutData> mListFilter = new ArrayList<>();
    private List<CustomSortOutData> mListAllWeight;
    private List<CustomSortOutData> mListAllCount;
    private List<CustomSortOutData> mListAll = new ArrayList<>();
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private SortOutAdapter mSortOutAdapter;
    private int mIntType = TYPE_WEIGHT;
    private boolean hasCancelSortOut;
    private CustomSortOutData mPreSortOutData;
    private HistorySortOutPopWindow mHistoryPopWindow;
    private DbImageUpload mDbImageUpload;
    private boolean mStable;
    private VideoFragment mVideoFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sort_out;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initContentView() {
        setEditShow("0.1");
        mTvTypeUnit.setText("kg");
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
        mSpWareHouse.setTitleColor(R.color.color_135c31);
        mSpCustomersLevel.setTitleColor(R.color.color_135c31);
        mSpCustomersLevel.setCustomItemSelectedListener(this);
        mSpWareHouse.setCustomItemSelectedListener(this);
        mEtCustomGroup.setOnInputFinishListener(this);
        mEtGoodsNameOrId.setOnInputFinishListener(this);
        mEtCustomers.setOnInputFinishListener(this);
        mBtnSortOut.setOnClickListener(new onNoDoubleClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                sortOut();
            }
        });
        mEtTag.setOnScanFinishListener(new onScanFinishListener() {
            @Override
            public void onScanFinish(String key) {
                if (!TextUtils.isEmpty(key)) {
                    if (mIvDelete.getVisibility() != View.VISIBLE) {
                        mIvDelete.setVisibility(View.VISIBLE);
                    }
                } else {
                    mIvDelete.setVisibility(View.GONE);
                }
                if (mListAllWeight == null || mListAllCount == null) {
                    ToastUtil.showShortToast(getContext(), "请先获取数据");
                    return;
                }
                showLoadingDialog(false);
                filterList();
                dismissLoadingDialog();
            }
        });
        mEtLeather.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (mIntType != TYPE_WEIGHT) {
                        return;
                    }
                    String leather = s.toString().trim();
                    if (!TextUtils.isEmpty(leather) && leather.equals(".")) {
                        String pre = "0.";
                        mEtLeather.setText(pre);
                        mEtLeather.setSelection(pre.length());
                        return;
                    }
                    String weight = mEtShow.getText().toString().trim();
                    BigDecimal w = new BigDecimal(0);
                    if (!TextUtils.isEmpty(weight)) {
                        w = new BigDecimal(weight);
                    }
                    BigDecimal l = new BigDecimal(0);
                    if (!TextUtils.isEmpty(leather)) {
                        l = new BigDecimal(leather);
                    }
                    String sub = BigDecimalUtil.toScaleStr(w.subtract(l));
                    mEtWeight.setText(sub);
                    sequenceListWeight(mListShow, sub);
                    scrollToBottom(0);
                } catch (Exception e) {
                    CrashReport.postCatchedException(e);
                }
            }
        });

        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        //数字键盘   InputFragment中设置mEtLeather文本焦点不在时不改变为白色
        InputFragment inputFragment = InputFragment.newInstance();
        inputFragment.setEditTexts(mEtShow, mEtLeather, mEtWeight);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mVideoFragment)
                .add(R.id.container, inputFragment)
                .hide(mVideoFragment)
                .hide(inputFragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void loadDate() {
        DbRefreshUtil.refreshRegist(this, new DbRefreshUtil.onDbRefreshListener() {
            @Override
            public void onRefresh() {
                if (mHistoryPopWindow != null && mHistoryPopWindow.isShowing()) {
                    refreshHistoryList();
                }
            }
        });

        getLocalInfo();
        mBtnSortOutCount.setSelected(false);
        mBtnSortOutWeight.setSelected(true);
        //设置当前日期
        String currentData = DateUtils.converToString(new Date());
        mBtnDate.setText(currentData);
    }

    private void notifyItemClick() {
        if (mIntType == TYPE_COUNT) {
            //显示数量
            BigDecimal hasStockOutQty = mPreSortOutData.getHasStockOutQty() != null ? mPreSortOutData.getHasStockOutQty() : new BigDecimal(0);
            String scaleStr = BigDecimalUtil.toScaleStr(mPreSortOutData.getGoodsQty().subtract(hasStockOutQty));
            setEditShow(scaleStr);
            mEtShow.setSelection(scaleStr.length());
            String unit = mPreSortOutData.getGoodsUnit().getName();
            mTvTypeUnit.setText(unit);
        } else {
            //设置为""后,点击每个item跳回到item=0
            //            setEditShow("");
            mTvTypeUnit.setText("kg");
        }
    }

    private void setEditShow(String show) {
        mEtShow.setText(show);
        mPreWeight = show;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbRefreshUtil.refreshUnRegist(this);
    }

    private void getLocalInfo() {
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(this);
        if (listWareHouse != null) {
            mSpWareHouse.setList(listWareHouse);
            int size = listWareHouse.size();
            for (int i = 0; i < size; i++) {
                Warehouse warehouse = listWareHouse.get(i);
                //默认配送仓
                if (warehouse.getCode().equals("003") && warehouse.getName().equals("配送仓")) {
                    mSpWareHouse.setSelection(i);
                }
            }
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
            mEtTag.setEnabled(true);
            mEtTag.requestFocus();
        } else {
            mEtTag.setEnabled(false);
            mEtGoodsNameOrId.requestFocus();
        }
    }

    @OnClick({R.id.btn_sort_out_weight, R.id.btn_sort_out_count, R.id.btn_request,
            R.id.btn_sort_out_history, R.id.btn_date, R.id.btn_clear_zero, R.id.iv_delete})
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
                mLlLeather.setVisibility(View.VISIBLE);
                mLlWeight.setVisibility(View.VISIBLE);
                mEtShow.setEnabled(false);
                setClick("称重数：", false, true);
                break;
            }
            case R.id.btn_sort_out_count: {
                if (mIntType == TYPE_COUNT) {
                    return;
                }
                mIntType = TYPE_COUNT;
                mLlLeather.setVisibility(View.GONE);
                mLlWeight.setVisibility(View.GONE);
                mEtLeather.setText("0");
                mEtWeight.setText("0");
                mEtShow.setEnabled(true);
                setClick("出库数量：", true, false);
                break;
            }
            case R.id.btn_sort_out_history:
                toHistory();
                break;
            case R.id.btn_request:
                refreshSortOutList();
                break;
            case R.id.btn_clear_zero:
                clearToZero();
                break;
            case R.id.iv_delete:
                String tag = mEtTag.getText().toString().trim();
                if (!TextUtils.isEmpty(tag)) {
                    mEtTag.setText("");
                }
                break;
            default:
                break;
        }
    }

    private void sortOut() {
        try {
            if (mPreSortOutData == null) {
                ToastUtil.showShortToast(getContext(), "请选择商品");
                return;
            }
            Warehouse selectedWareHouse = mSpWareHouse.getSelectedItem();
            if (selectedWareHouse == null) {
                ToastUtil.showShortToast(getContext(), "请选择仓库");
                return;
            }
            String countStr;
            if (mPreSortOutData.getUnitCoefficient() != null && mPreSortOutData.getUnitCoefficient().doubleValue() != 0) {
                countStr = mEtWeight.getText().toString().trim();
            } else {
                countStr = mEtShow.getText().toString().trim();
            }
            if (TextUtils.isEmpty(countStr) || Double.parseDouble(countStr) == 0) {
                if (mIntType == TYPE_WEIGHT) {
                    ToastUtil.showShortToast(getContext(), "请称重");
                } else {
                    ToastUtil.showShortToast(getContext(), "请输入数量");
                }
                return;
            }
            mBtnSortOut.setEnabled(false);
            if (mVideoFragment.isLight()) {
                mVideoFragment.screenshot(mHandlerShotPic);
            } else {
                showLoadingDialog(false);
                setDataToDb(null);
                setBtnEnable();
                dismissLoadingDialog();
            }
        } catch (Exception e) {
            BuglyUtil.uploadCrash(e);
        }
    }

    private void refreshSortOutList() {
        String time = mBtnDate.getText().toString().trim();
        if (!TextUtils.isEmpty(time)) {
            mBtnRequest.setEnabled(false);
            dismissLoadingDialog();
            showLoadingDialog(false);
            mListAll.clear();
            mListAllWeight = null;
            mListAllCount = null;
            mListFilter.clear();
            mListShow.clear();
            mSortOutAdapter.notifyDataSetChanged();

            requestListWeight(time);
            requestListCount(time);

        }
    }

    /**
     * 获取分拣数据-重量
     */
    private void requestListWeight(String time) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put(Common.PAGE_STRING, Common.PAGE);
            params.put(Common.PAGE_SIZE_STRING, Common.PAGE_SIZE);
            params.put(Common.DEFAULT_PAGE_SIZE_STRING, Common.DEFAULT_PAGE_SIZE);
            params.put("DATE", time);
            getIRequestDataService().onGetDataListener(
                    RequestDataService.TYPE_SORT_OUT_WEIGHT,
                    params,
                    new OnRequestDataListener.Stub() {
                        @Override
                        public void onReceive(long type) throws RemoteException {
                            Message msg = MessageUtil.create(type, null);
                            mRequestData.sendMessage(msg);
                        }

                        @Override
                        public void onError(long type, String message) throws RemoteException {
                            mRequestData.sendMessage(MessageUtil.create(type, message));
                        }

                    }
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分拣数据-数量
     */
    private void requestListCount(String time) {
        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put(Common.PAGE_STRING, Common.PAGE);
            params.put(Common.PAGE_SIZE_STRING, Common.PAGE_SIZE);
            params.put(Common.DEFAULT_PAGE_SIZE_STRING, Common.DEFAULT_PAGE_SIZE);
            params.put("DATE", time);
            getIRequestDataService().onGetDataListener(
                    RequestDataService.TYPE_SORT_OUT_COUNT,
                    params,
                    new OnRequestDataListener.Stub() {
                        @Override
                        public void onReceive(long type) throws RemoteException {
                            Message msg = MessageUtil.create(type, null);
                            mRequestData.sendMessage(msg);
                        }

                        @Override
                        public void onError(long type, String message) throws RemoteException {
                            mRequestData.sendMessage(MessageUtil.create(type, message));
                        }

                    }
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onGetListSuccess() {
        if (mListAllCount != null && mListAllWeight != null) {
            //删除后台未上传的数据
            removeUnSortOut();
            //筛选条件
            filterList();
            dismissLoadingDialog();
            mBtnRequest.setEnabled(true);
        }
    }

    private void onGetListFailed(String message) {
        dismissLoadingDialog();
        mBtnRequest.setEnabled(true);
        if (!TextUtils.isEmpty(message)) {
            ToastUtil.showShortToast(getContext(), message);
        }
    }

    private void setClick(String type, boolean countSelect, boolean weightSelect) {
        mTvType.setText(type);
        setEditShow("");
        mBtnSortOutCount.setSelected(countSelect);
        mBtnSortOutWeight.setSelected(weightSelect);
        mListFilter.clear();
        mListShow.clear();
        mSortOutAdapter.notifyDataSetChanged();
        filterList();
    }

    private Handler mRequestData = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long type = MessageUtil.getType(msg);
            if (type == RequestDataService.TYPE_SORT_OUT_WEIGHT) {
                List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
                String data = dbRequestData.get(0).getData();
                if (!TextUtils.isEmpty(data)) {
                    List<CustomSortOutData> list = GsonUtil.getGson().fromJson(data, new TypeToken<List<CustomSortOutData>>() {
                    }.getType());
                    mListAllWeight = new ArrayList<>();
                    mListAllWeight.clear();
                    mListAllWeight.addAll(list);
                    mListAll.addAll(mListAllWeight);
                    onGetListSuccess();
                }
            } else if (type == RequestDataService.TYPE_SORT_OUT_WEIGHT_FAILED) {
                String data = (String) msg.obj;
                onGetListFailed(data);
            } else if (type == RequestDataService.TYPE_SORT_OUT_COUNT) {
                List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
                String data = dbRequestData.get(0).getData();
                if (!TextUtils.isEmpty(data)) {
                    List<CustomSortOutData> list = GsonUtil.getGson().fromJson(data, new TypeToken<List<CustomSortOutData>>() {
                    }.getType());
                    mListAllCount = new ArrayList<>();
                    mListAllCount.clear();
                    mListAllCount.addAll(list);
                    mListAll.addAll(mListAllCount);
                    onGetListSuccess();
                }
            } else if (type == RequestDataService.TYPE_SORT_OUT_COUNT_FAILED) {
                String data = (String) msg.obj;
                onGetListFailed(data);
            } else if (type == RequestDataService.TYPE_SORT_OUT_CANCEL) {
                onCancelSuccess(mDbImageUpload);
            } else if (type == RequestDataService.TYPE_SORT_OUT_CANCEL_FAILED) {
                dismissLoadingDialog();
                mDbImageUpload = null;
                String data = (String) msg.obj;
                if (!TextUtils.isEmpty(data)) {
                    ToastUtil.showShortToast(getContext(), data);
                }
            }
            return false;
        }
    });

    private Handler mHandlerShotPic = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    showLoadingDialog(false);
                    setBtnEnable();
                    break;
                case 2:
                    //保存图片路径都后台作为请求
                    String path = msg.getData().getString("path", "");
                    setDataToDb(path);
                    setBtnEnable();
                    dismissLoadingDialog();
                    break;
                case 3:
                    setDataToDb(null);
                    setBtnEnable();
                    dismissLoadingDialog();
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
    private void setDataToDb(String imagePath) {
        if (mPreSortOutData != null) {
            // 保存 称重数（订购数量）   直接上传kg数   不需要转换
            boolean isWeight = mPreSortOutData.getUnitCoefficient() != null && mPreSortOutData.getUnitCoefficient().doubleValue() != 0;
            if (isWeight) {
                String countStr = mEtWeight.getText().toString().trim();
                mPreSortOutData.setStockOutQty(new BigDecimal(countStr).divide(mPreSortOutData.getUnitCoefficient(), RoundingMode.HALF_EVEN));
            } else {
                String countStr = mEtShow.getText().toString().trim();
                mPreSortOutData.setStockOutQty(new BigDecimal(countStr));
            }
            //去皮
            String leather = mEtLeather.getText().toString().trim();
            if (!TextUtils.isEmpty(leather)) {
                mPreSortOutData.setLeatherQty(new BigDecimal(leather));
            } else {
                mPreSortOutData.setLeatherQty(new BigDecimal(0));
            }
            // 保存 仓库
            Warehouse warehouse = mSpWareHouse.getSelectedItem();
            if (warehouse != null) {
                mPreSortOutData.setWarehouse(new UCN(warehouse.getUuid(), warehouse.getCode(), warehouse.getName()));
            }
            // 生成追溯码
            mPreSortOutData.setPlatformTraceCode(Common.getPlatformTraceCode());
            //如果是扫描库存标签过滤出来的列表，设置basketCode
            String labelCode = mPreSortOutData.getLabelCode();
            if (!TextUtils.isEmpty(labelCode)) {
                mPreSortOutData.setBasketCode(labelCode);
                mPreSortOutData.setLabelCode(null);
            }

            //总出库数   hasStockOutQty总是有值，不用判空
            BigDecimal unitCoefficient = mPreSortOutData.getUnitCoefficient();

            mPreSortOutData.setImages(Arrays.asList(GetImageFile.getName(imagePath)));

            // ** 保存数据到数据库在 setStockOutQty，setWarehouse 后
            DbImageUpload dbImageUpload = new DbImageUpload();
            dbImageUpload.setDate(mBtnDate.getText().toString().trim());
            dbImageUpload.setOperatime(DateUtils.getTime2(new Date()));
            dbImageUpload.setLine(GsonUtil.getGson().toJson(mPreSortOutData));
            dbImageUpload.setImagePath(imagePath);
            dbImageUpload.setType(Common.DbType.TYPE_SORT_OUT_STORE_OUT);
            getDbManager().insertDbImageUpload(dbImageUpload);

            //界面不显示
            //从列表中删除
            removeCurrentItem(mPreSortOutData, mListShow);
            removeCurrentItem(mPreSortOutData, mListFilter);
            removeCurrentItem(mPreSortOutData, mListAll);
            if (mIntType == TYPE_WEIGHT) {
                removeCurrentItem(mPreSortOutData, mListAllWeight);
            } else if (mIntType == TYPE_COUNT) {
                removeCurrentItem(mPreSortOutData, mListAllCount);
            }

            //打印标签
            printer(unitCoefficient);
            //先打印后清除数据
            setParamsNull(mPreSortOutData);
            if (mListShow.size() > 0) {
                mPreSortOutData = mListShow.get(0);
                //默认设置第一个
                mSortOutAdapter.setIntSelect(0);
                mSortOutAdapter.notifyDataSetChanged();
                notifyItemClick();
            } else if (mListShow.size() == 0) {
                mPreSortOutData = null;
                mTvTypeUnit.setText("");
            }
            if (mIntType == TYPE_WEIGHT) {
                if (!TextUtils.isEmpty(leather)) {
                    mEtLeather.setText("");
                }
            }
            ToastUtil.showShortToast(getContext(), "分拣成功");
        }
    }

    /**
     * 删除当前选中的item
     */
    private void removeCurrentItem(CustomSortOutData data, List<CustomSortOutData> list) {
        int indexShow = getListIndex(data, list);
        if (indexShow != -1) {
            list.remove(indexShow);
        }
    }

    /**
     * 获取列表中与当前选中项相同SourceBillLineUuid的下标
     */
    private int getListIndex(CustomSortOutData data, List<CustomSortOutData> list) {
        int index = -1;
        if (list != null && data != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                CustomSortOutData sortOutData = list.get(i);
                if (sortOutData.getSourceBillLineUuid().equals(data.getSourceBillLineUuid())) {
                    return i;
                }
            }
        }
        return index;
    }

    private void printer(BigDecimal unitCoefficient) {
        // 打印标签
        String goodsName = mPreSortOutData.getGoods().getName();
        String sortOutNum;
        if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
            sortOutNum = BigDecimalUtil.toScaleStr(mPreSortOutData.getStockOutQty().multiply(unitCoefficient)).concat("kg");
        } else {
            sortOutNum = BigDecimalUtil.toScaleStr(mPreSortOutData.getStockOutQty()).concat(mPreSortOutData.getGoodsUnit().getName());
        }
        String code = mPreSortOutData.getPlatformTraceCode();
        String customer = mPreSortOutData.getCustomer() != null ? mPreSortOutData.getCustomer().getName() : "";
        String department = mPreSortOutData.getCustomerDepartment() != null ? mPreSortOutData.getCustomerDepartment().getName() : "";
        String storageMode = mPreSortOutData.getStorageMode();
        String period = mPreSortOutData.getPeriod();
        PrinterBean printerBean = PrinterBean.get(1, code, customer, department, goodsName, sortOutNum, storageMode, period);
        PrinterSortOut.printer(
                getContext(),
                printerBean);
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
            mHistoryPopWindow = new HistorySortOutPopWindow(this, mBtnSortOutHistory);
            mHistoryPopWindow.setOnDeleteListener(this);
            mHistoryPopWindow.setOnDismissListener(this);
        }
        refreshHistoryList();
    }

    @Override
    public void onDismiss() {
        if (hasCancelSortOut) {
            mListShow.clear();
            mListFilter.clear();
            filterList();
        }
        hasCancelSortOut = false;
    }

    /**
     * 刷新历史列表
     */
    private void refreshHistoryList() {
        //获取数据库当天的数据
        String date = mBtnDate.getText().toString().trim();
        List<DbImageUpload> daoList = getDbManager().getDbListSortOutStoreOutAll(date);
        Collections.reverse(daoList);
        mListHistory.clear();
        mListHistory.addAll(daoList);
        mHistoryPopWindow.show();
        mHistoryPopWindow.notify(mListHistory);
    }

    @Override
    public void delete(DbImageUpload dbImageUpload) {
        mDbImageUpload = dbImageUpload;
        showLoadingDialog(false);
        String stockOutUuid = dbImageUpload.getStockOutUuid();
        if (!TextUtils.isEmpty(stockOutUuid)) {
            //已分拣 请求接口撤销
            CustomSortOutData data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), CustomSortOutData.class);
            //再次保证StockOutRecordUuids有值
            List<String> stockOutRecordUuids = data.getStockOutRecordUuids();
            if (stockOutRecordUuids == null) {
                stockOutRecordUuids = new ArrayList<>();
            }
            if (stockOutRecordUuids.size() == 0) {
                stockOutRecordUuids.add(stockOutUuid);
                data.setStockOutRecordUuids(stockOutRecordUuids);
            }
            cancelOnLine(data);
        } else {
            onCancelSuccess(dbImageUpload);
        }
    }

    /**
     * 获取分拣数据-取消
     */
    private void cancelOnLine(CustomSortOutData data) {
        long type = RequestDataService.TYPE_SORT_OUT_CANCEL;
        HashMap<String, CustomSortOutData> param = new HashMap<>();
        param.put("CustomSortOutData", data);
        try {
            getIRequestDataService().onGetDataListener(type, param, new OnRequestDataListener.Stub() {
                @Override
                public void onReceive(long type) throws RemoteException {
                    Message msg = MessageUtil.create(type, null);
                    mRequestData.sendMessage(msg);
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

    private void onCancelSuccess(DbImageUpload dbImageUpload) {
        //先刷新后删除数据库
        refreshCancelSortOut(dbImageUpload);
        getDbManager().deleteDbImageUpload(dbImageUpload);
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销分拣成功");
        hasCancelSortOut = true;
        dismissLoadingDialog();
        mDbImageUpload = null;
    }

    private void refreshCancelSortOut(DbImageUpload dbImageUpload) {
        if (dbImageUpload == null || mListAllWeight == null || mListAllCount == null) {
            return;
        }
        //判断分拣的时间 是否跟 现在选择的时间一样
        String date = mBtnDate.getText().toString().trim();
        if (!dbImageUpload.getDate().equals(date)) {
            return;
        }
        CustomSortOutData data = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), CustomSortOutData.class);
        String dataSourceBillLineUuid = data.getSourceBillLineUuid();
        BigDecimal unitCoefficient = data.getUnitCoefficient();
        CustomSortOutData currentCancelData = null;
        if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
            // 重量列表
            for (CustomSortOutData d : mListAllWeight) {
                if (d.getSourceBillLineUuid().equals(dataSourceBillLineUuid)) {
                    //还存在
                    currentCancelData = d;
                    break;
                }
            }
            if (currentCancelData != null) {
                // 直接添加到item中
                setParamsNull(currentCancelData);
                BigDecimal stockOutQty = data.getStockOutQty();
                BigDecimal coverToKgQty = currentCancelData.getCoverToKgQty();
                currentCancelData.setCoverToKgQty(coverToKgQty.add(stockOutQty.multiply(unitCoefficient)));
                BigDecimal hasStockOutQty = currentCancelData.getHasStockOutQty();
                currentCancelData.setHasStockOutQty(hasStockOutQty.subtract(stockOutQty));
            } else {
                // 重新添加   遍历下历史中所有的该SourceBillLineUuid相同的订单
                setParamsNull(data);
                mListAllWeight.add(data);
                mListAll.add(data);
            }
        } else {
            // 数量
            for (CustomSortOutData d : mListAllCount) {
                if (d.getSourceBillLineUuid().equals(dataSourceBillLineUuid)) {
                    //还存在
                    currentCancelData = d;
                    break;
                }
            }
            if (currentCancelData != null) {
                // 撤销的数量
                setParamsNull(currentCancelData);
                BigDecimal stockOutQty = data.getStockOutQty();
                BigDecimal hasStockOutQty = currentCancelData.getHasStockOutQty();
                currentCancelData.setHasStockOutQty(hasStockOutQty.subtract(stockOutQty));
                if (currentCancelData.getStockOutQty() != null) {
                    currentCancelData.setStockOutQty(currentCancelData.getStockOutQty().add(data.getStockOutQty()));
                } else {
                    currentCancelData.setStockOutQty(data.getStockOutQty());
                }
            } else {
                // 重新添加   遍历下历史中所有的该SourceBillLineUuid相同的订单
                setParamsNull(data);
                mListAllCount.add(data);
                mListAll.add(data);
            }
        }
    }

    private void setParamsNull(CustomSortOutData data) {
        data.setPlatformTraceCode(null);
        data.setWarehouse(null);
        data.setStockOutRecordUuids(null);
        data.setImages(null);
        data.setStockOutQty(null);
        data.setLeatherQty(null);
    }

    /**
     * 扣除数据库中未上传的数据
     */
    private void removeUnSortOut() {
        if (mListAllWeight == null || mListAllCount == null) {
            return;
        }
        //获取数据库中未上传的数据   扣除数据库中未上传的数据
        String date = mBtnDate.getText().toString();
        //包含已上传跟未上传的数据， 有可能请求的时候后台数据还没更新
        List<DbImageUpload> dbListSortOutStoreOut = getDbManager().getDbListSortOutStoreOutAll(date);
        Map<String, DbImageUpload> listUnUpload = new HashMap<>();
        for (DbImageUpload dbImageUpload : dbListSortOutStoreOut) {
            CustomSortOutData dbCso = GsonUtil.getGson().fromJson(dbImageUpload.getLine(), CustomSortOutData.class);
            listUnUpload.put(dbCso.getSourceBillLineUuid(), dbImageUpload);
        }
        Iterator<CustomSortOutData> iteratorAll = mListAll.iterator();
        while (iteratorAll.hasNext()) {
            CustomSortOutData data = iteratorAll.next();
            boolean containsKey = listUnUpload.containsKey(data.getSourceBillLineUuid());
            if (containsKey) {
                BigDecimal unitCoefficient = data.getUnitCoefficient();
                iteratorAll.remove();
                if (unitCoefficient != null && unitCoefficient.doubleValue() != 0) {
                    removeCurrentItem(data, mListAllWeight);
                } else {
                    removeCurrentItem(data, mListAllCount);
                }
            }
        }
    }

    @Override
    protected void onLoadingDismiss() {
        super.onLoadingDismiss();
        mBtnRequest.setEnabled(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
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
        if (mListAllWeight == null || mListAllCount == null) {
            return;
        }
        if ((mIntType == TYPE_WEIGHT && mListAllWeight.size() == 0)
                ||
                (mIntType == TYPE_COUNT && mListAllCount.size() == 0)) {
            mListFilter.clear();
            mListShow.clear();
            scrollToBottom(0);
            return;
        }
        CustomerLevel customerLevel = mSpCustomersLevel.getSelectedItem();
        String goodsNameOrId = mEtGoodsNameOrId.getText().toString().trim();
        String customGroup = mEtCustomGroup.getText().toString().trim();
        String customer = mEtCustomers.getText().toString().trim();
        String tag = mEtTag.getText().toString().trim();
        //逐级删除不符合的数据
        ArrayList<CustomSortOutData> filter;
        if (mIntType == TYPE_WEIGHT) {
            //重量
            filter = FilterUtil.filter(mListAllWeight, customerLevel, customer, customGroup, goodsNameOrId, tag);
        } else {
            //数量
            filter = FilterUtil.filter(mListAllCount, customerLevel, customer, customGroup, goodsNameOrId, tag);
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
        //        scrollToItem(0);
        if (mIntType == TYPE_WEIGHT) {
            //稳定后 排序
            if (mStable) {
                String weightStr = mEtShow.getText().toString().trim();
                String weight = !TextUtils.isEmpty(weightStr) ? weightStr : "0";
                BigDecimal w = new BigDecimal(weight);
                String leather = mEtLeather.getText().toString().trim();
                BigDecimal l;
                if (!TextUtils.isEmpty(leather)) {
                    l = new BigDecimal(Double.parseDouble(leather));
                } else {
                    l = new BigDecimal(0);
                }
                String sub = BigDecimalUtil.toScaleStr(w.subtract(l));
                sequenceListWeight(mListShow, sub);
            }
        }
        scrollToBottom(0);
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
                scrollToBottom(0);
                ToastUtil.showShortToast(getContext(), "该周转筐没有商品");
                dismissLoadingDialog();
                return;
            }
            int size = mListShow.size();
            //筛选出Stock的uuid跟SortOutData的uuid一样的商品
            Stock stock = stocks.get(0);
            ArrayList<CustomSortOutData> listFilter = null;
            for (int i = 0; i < size; i++) {
                CustomSortOutData data = mListShow.get(i);
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
            scrollToBottom(0);
            dismissLoadingDialog();
        } else {
            dismissLoadingDialog();
        }
    }

    @Override
    public void onQueryStockFailed(int errorType, String failString) {
        ToastUtil.showShortToast(getContext(), failString);
        dismissLoadingDialog();
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
                //                if (mIntType == TYPE_WEIGHT) {
                //                    String weight = mEtShow.getText().toString().trim();
                //                    if (!TextUtils.isEmpty(weight)) {
                //                        sequenceListWeight(mListShow, weight);
                //                    }
                //                }
                mSortOutAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 商品列表适配器
     */
    private class SortOutAdapter extends CommonAdapter4Lv<CustomSortOutData> {

        private int mIntSelect;
        private float mDim10Sp;

        private SortOutAdapter(Context context, List<CustomSortOutData> data) {
            super(context, R.layout.item_sort_out, data);
            mDim10Sp = getResources().getDimension(R.dimen._10sp);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, CustomSortOutData data, int position) {
            try {
                if (mIntSelect == position) {
                    holder.setBackgroundRes(R.id.item_ll, R.color.color_ffe080);
                } else {
                    holder.setBackgroundRes(R.id.item_ll, R.color.color_e7f0d5);
                }
                if (mIntType == TYPE_WEIGHT) {
                    BigDecimal coverToKgQty = data.getCoverToKgQty();
                    if (coverToKgQty != null) {
                        holder.setText(R.id.item_goods_weight, BigDecimalUtil.toScaleStr(coverToKgQty));
                        holder.setText(R.id.item_goods_weight_unit, "kg");
                    }
                } else if (mIntType == TYPE_COUNT) {
                    holder.setText(R.id.item_goods_weight, BigDecimalUtil.toScaleStr(data.getGoodsQty().subtract(data.getHasStockOutQty())));
                    IdName goodsUnit = data.getGoodsUnit();
                    if (goodsUnit != null) {
                        String unit = goodsUnit.getName();
                        holder.setText(R.id.item_goods_weight_unit, unit);
                    } else {
                        holder.setText(R.id.item_goods_weight_unit, "");
                    }
                }
                //设置备注
                holder.setText(R.id.item_goods_remark, data.getRemark());
                ScalableTextView tvName = holder.getView(R.id.item_goods_name);
                tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDim10Sp);
                tvName.setText(data.getGoods().getName());
                ScalableTextView tvCustomer = holder.getView(R.id.item_goods_customer);
                tvCustomer.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDim10Sp);
                holder.setText(R.id.item_goods_customer, data.getCustomer().getName());
            } catch (Exception e) {
                e.printStackTrace();
                BuglyUtil.uploadCrash(e);
            }
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
        try {
            String weight = data.weight;
            if ((mIntType == TYPE_WEIGHT) && !TextUtils.isEmpty(weight)) {
                mStable = data.stable;
                mEtShow.setText(weight);
                BigDecimal w = new BigDecimal(Double.parseDouble(weight));
                String leather = mEtLeather.getText().toString().trim();
                BigDecimal l;
                if (!TextUtils.isEmpty(leather)) {
                    l = new BigDecimal(leather);
                } else {
                    l = new BigDecimal(0);
                }
                if (mStable && !weight.equals(mPreWeight)) {
                    mPreWeight = weight;
                    mEtWeight.setText(BigDecimalUtil.toScaleStr(w.subtract(l)));
                    String strW = String.valueOf(w.subtract(l));
                    sequenceListWeight(mListShow, strW);
                    scrollToBottom(0);
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            BuglyUtil.uploadCrash(e);
        }
    }

    /**
     * 根据称重数据进行排序，以称重数量差额的绝对值升序；
     */
    private ListComparator mListComparator;

    private void sequenceListWeight(List<CustomSortOutData> list, String weight) {
        if (!TextUtils.isEmpty(weight) && list.size() > 0) {
            double douWeight = Double.parseDouble(weight);
            if (mListComparator != null) {
                mListComparator.setDobWeight(douWeight);
            } else {
                mListComparator = new ListComparator(douWeight);
            }
            Collections.sort(list, mListComparator);
            mSortOutAdapter.notifyDataSetChanged();
        }
    }

    private void scrollToBottom(int position) {
        if (mListShow.size() > 0) {
            scrollToItem(position);
        }
    }

    private void scrollToItem(int itemPosition) {
        mPreSortOutData = mListShow.get(itemPosition);
        mSortOutAdapter.setIntSelect(itemPosition);
        mSortOutAdapter.notifyDataSetChanged();
        mGvSortOut.smoothScrollToPosition(itemPosition);
    }

    @Override
    public String getBaseTitle() {
        return "分拣";
    }
}
