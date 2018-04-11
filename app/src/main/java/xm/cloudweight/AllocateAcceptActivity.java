package xm.cloudweight;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
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
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.xmzynt.storm.basic.idname.IdName;
import com.xmzynt.storm.basic.ucn.UCN;
import com.xmzynt.storm.service.wms.allocate.AllocateRecord;
import com.xmzynt.storm.service.wms.stockin.StockInRecord;
import com.xmzynt.storm.service.wms.warehouse.Warehouse;
import com.xmzynt.storm.util.GsonUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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
import xm.cloudweight.fragment.InputFragment;
import xm.cloudweight.fragment.VideoFragment;
import xm.cloudweight.utils.BigDecimalUtil;
import xm.cloudweight.utils.DateUtils;
import xm.cloudweight.utils.KeyBoardUtils;
import xm.cloudweight.utils.ToastUtil;
import xm.cloudweight.utils.bussiness.DatePickerDialogUtil;
import xm.cloudweight.utils.bussiness.MessageUtil;
import xm.cloudweight.utils.bussiness.printer.PrinterBean;
import xm.cloudweight.utils.bussiness.printer.PrinterInventory;
import xm.cloudweight.utils.dao.DbRefreshUtil;
import xm.cloudweight.utils.dao.bean.DbImageUpload;
import xm.cloudweight.utils.dao.bean.DbRequestData;
import xm.cloudweight.widget.BaseTextWatcher;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;
import xm.cloudweight.widget.DataSpinner;
import xm.cloudweight.widget.HistoryAllocateAcceptPopWindow;
import xm.cloudweight.widget.ScanEditText;
import xm.cloudweight.widget.SearchAndFocusEditText;
import xm.cloudweight.widget.impl.OnDeleteListener;
import xm.cloudweight.widget.impl.onInputFinishListener;
import xm.cloudweight.widget.impl.onScanFinishListener;

import static java.math.BigDecimal.ROUND_HALF_EVEN;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_CANCEL;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_CANCEL_FAILED;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA_FAILED;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK;
import static xm.cloudweight.service.RequestDataService.TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK_FAILED;

/**
 * @author : wyh
 * @create : 2018/2/26
 * @des :  调拨验收
 */
public class AllocateAcceptActivity extends BaseActivity implements VideoFragment.OnInstrumentListener, AdapterView.OnItemSelectedListener, OnDeleteListener {

    private VideoFragment mVideoFragment;
    private InputFragment mInputFragment;
    @BindView(R.id.btn_date)
    Button mBtnDate;
    @BindView(R.id.sp_ware_house_in)
    DataSpinner<Warehouse> mSpWareHouseIn;
    @BindView(R.id.et_inventory_label)
    ScanEditText mEtInventoryLabel;
    @BindView(R.id.et_print_label_count)
    EditText mEtPrintLabelCount;
    @BindView(R.id.btn_allocate_accept_submit)
    Button mBtnAllocateAcceptSubmit;
    @BindView(R.id.tv_info_goods)
    TextView mTvInfoGood;
    @BindView(R.id.tv_info_num)
    TextView mTvInfoNum;
    @BindView(R.id.tv_info_remark)
    TextView mTvInfoRemark;
    @BindView(R.id.lv_line)
    ListView mLvNotAccpet;
    @BindView(R.id.et_key_search)
    SearchAndFocusEditText mEtKeySearch;
    private List<AllocateRecord> mListShow = new ArrayList<>();
    private List<AllocateRecord> mListAll = new ArrayList<>();
    private Map<String, String> mMapAccumulate = new HashMap<>();
    private List<DbImageUpload> mListHistory = new ArrayList<>();
    private EditText mEtWeightCurrent;
    private SearchAndFocusEditText mEtWeightAccumulate;
    private SearchAndFocusEditText mEtBucklesLeather;
    private SearchAndFocusEditText mEtDeductWeight;
    private EditText mEtNumWarehousing;
    private TextView mTvNumWarehousingUnit;
    private TextView mTvWeightAccumulateUnit;
    private ImageView mIvSub;
    private ImageView mIvAdd;
    private NotAcceptAdapter mNotAcceptAdapter;
    private AllocateRecord mAllocateRecord;
    private HistoryAllocateAcceptPopWindow mAllocateAcceptPopWindow;
    private DbImageUpload mDbImageUpload;
    private boolean hasCancelAccept;

    @Override
    public String getBaseTitle() {
        return "调拨验收";
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_allocate_accept;
    }

    @Override
    protected void initContentView() {
        //请求焦点  解决监听EditText问题
        findViewById(R.id.activity_allocate_accept).requestFocus();
        mEtWeightCurrent = (EditText) getEditText(R.id.ll_weight_current, "当前重量");
        mEtWeightCurrent.setEnabled(false);
        mEtWeightCurrent.setBackground(null);
        mEtWeightCurrent.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtWeightAccumulate = (SearchAndFocusEditText) getEditText(R.id.ll_weight_accumulate, "累计重量");
        mTvWeightAccumulateUnit = ((TextView) findViewById(R.id.ll_weight_accumulate).findViewById(R.id.tv_unit));
        mEtWeightAccumulate.setEnabled(false);
        mEtWeightAccumulate.setBackground(null);
        mEtWeightCurrent.setTextSize(getResources().getDimension(R.dimen._16sp));
        mEtBucklesLeather = (SearchAndFocusEditText) getEditText(R.id.ll_buckles_leather, "扣皮");
        mEtDeductWeight = (SearchAndFocusEditText) getEditText(R.id.ll_deduct_weight, "扣重");
        mEtNumWarehousing = (EditText) getEditText(R.id.ll_num_warehousing, "入库数量");
        mTvNumWarehousingUnit = ((TextView) findViewById(R.id.ll_num_warehousing).findViewById(R.id.tv_unit));
        mIvSub = findViewById(R.id.ll_num_warehousing).findViewById(R.id.img_sub);
        mIvAdd = findViewById(R.id.ll_num_warehousing).findViewById(R.id.img_add);

        mNotAcceptAdapter = new NotAcceptAdapter(getContext(), mListShow);
        mLvNotAccpet.setAdapter(mNotAcceptAdapter);
        mLvNotAccpet.setOnItemClickListener(mOnItemClickListener);

        //设置监听
        mEtInventoryLabel.setOnScanFinishListener(mLabelOnScanFinishListener);
        mSpWareHouseIn.setCustomItemSelectedListener(this);
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
                    weightCoefficient = mAllocateRecord.getWeightCoefficient();
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
                    weightCoefficient = mAllocateRecord.getWeightCoefficient();
                }
                BigDecimal finallyNum = currentWeight.subtract(leather).subtract(deduct).divide(weightCoefficient, RoundingMode.HALF_EVEN);
                mEtNumWarehousing.setText(BigDecimalUtil.toScaleStr(finallyNum));
            }
        });

        //初始化视频
        mVideoFragment = VideoFragment.getInstance();
        mVideoFragment.setInstrumentListener(this);
        //禁止弹软键盘
        mInputFragment = InputFragment.newInstance();
        mInputFragment.setEditTexts(mEtBucklesLeather, mEtDeductWeight, mEtNumWarehousing);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, mVideoFragment)
                .add(R.id.container, mInputFragment)
                .hide(mInputFragment)
                .show(mVideoFragment)
                .commitAllowingStateLoss();

        DbRefreshUtil.refreshRegist(this, new DbRefreshUtil.onDbRefreshListener() {
            @Override
            public void onRefresh() {
                if (mAllocateAcceptPopWindow != null && mAllocateAcceptPopWindow.isShowing()) {
                    refreshHistoryList();
                }
            }
        });

    }

    @Override
    protected void loadDate() {
        //设置当前日期
        String currentData = DateUtils.StringData();
        mBtnDate.setText(currentData);
        //获取仓库验收列表
        showLoadingDialog(true);
        getWareHouseCheck();
    }

    /**
     * 接口请求对应的回调
     */
    private Handler mRequestData = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            long type = MessageUtil.getType(msg);
            if (type == TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK) {
                getWareHouseCheckSuccess(type);
            } else if (type == TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK_FAILED) {
                getWareHouseCheckFailed(msg);
            } else if (type == TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA) {
                queryNotAcceptDataSuccess(type);
            } else if (type == TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA_FAILED) {
                queryNotAcceptDataFailed(msg);
            } else if (type == TYPE_ALLOCATE_ACCEPT_CANCEL) {
                onCancelSuccess();
            } else if (type == TYPE_ALLOCATE_ACCEPT_CANCEL_FAILED) {
                onCancelFailed(msg);
            }
            return false;
        }
    });

    /**
     * 获取仓库验收列表
     */
    private void getWareHouseCheck() {
        try {
            getIRequestDataService().onGetDataListener(TYPE_ALLOCATE_ACCEPT_WARE_HOUSE_CHECK, null, new OnRequestDataListener.Stub() {
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
     * 获取仓库验收列表成功
     */
    private void getWareHouseCheckSuccess(long type) {
        List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
        String data = dbRequestData.get(0).getData();
        List<Warehouse> result = GsonUtil.getGson().fromJson(data, new TypeToken<List<Warehouse>>() {
        }.getType());
        if (result != null && result.size() > 0) {
            mSpWareHouseIn.setList(result);
            String warehouseUuid = result.get(0).getUuid();
            queryNotAcceptData(warehouseUuid);
        } else {
            dismissLoadingDialog();
            ToastUtil.showShortToast(getContext(), "无仓库验收列表数据");
        }
    }

    /**
     * 获取仓库验收列表失败
     */
    private void getWareHouseCheckFailed(Message msg) {
        String failedMsg = MessageUtil.getObj(msg);
        ToastUtil.showShortToast(getContext(), failedMsg);
        dismissLoadingDialog();
    }

    /**
     * 获取未验收商品列表
     */
    private void queryNotAcceptData(String warehouseUuid) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", 0);
            params.put("pageSize", 0);
            params.put("defaultPageSize", 0);
            params.put("date", mBtnDate.getText().toString());
            params.put("status", "unAccept");
            params.put("inWarehouseUuid", warehouseUuid);
            getIRequestDataService().onGetDataListener(TYPE_ALLOCATE_ACCEPT_QUERY_NOT_ACCEPT_DATA, params, new OnRequestDataListener.Stub() {
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
     * 获取未验收商品列表成功
     */
    private void queryNotAcceptDataSuccess(long type) {
        List<DbRequestData> dbRequestData = getDbRequestDataManager().getDbRequestData(type);
        String data = dbRequestData.get(0).getData();
        List<AllocateRecord> result = GsonUtil.getGson().fromJson(data, new TypeToken<List<AllocateRecord>>() {
        }.getType());
        if (result != null && result.size() > 0) {
            mListAll.clear();
            mListAll.addAll(result);
            filterList();
        } else {
            mListShow.clear();
            mListAll.clear();
            mNotAcceptAdapter.notifyDataSetChanged();
            mAllocateRecord = null;
        }
        dismissLoadingDialog();
    }

    /**
     * 获取未验收商品列表失败
     */
    private void queryNotAcceptDataFailed(Message msg) {
        String failedMsg = MessageUtil.getObj(msg);
        ToastUtil.showShortToast(getContext(), failedMsg);
        dismissLoadingDialog();
    }

    /**
     * 过滤列表
     */
    private void filterList() {
        if (mListAll.size() > 0) {
            String key = mEtKeySearch.getText().toString().trim();
            List<AllocateRecord> listKeyName = new ArrayList<>();
            if (!TextUtils.isEmpty(key)) {
                for (AllocateRecord data : mListAll) {
                    UCN goods = data.getGoods();
                    if (goods != null && goods.getName().contains(key)) {
                        listKeyName.add(data);
                    }
                }
            } else {
                listKeyName.addAll(mListAll);
            }
            mListShow.clear();
            mListShow.addAll(listKeyName);
            mNotAcceptAdapter.notifyDataSetChanged();
        } else {
            mListShow.clear();
            mNotAcceptAdapter.notifyDataSetChanged();
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

    /**
     * 清空商品列表
     */
    private void clearGoodsList() {
        if (mListAll.size() > 0) {
            mListAll.clear();
        }
        if (mListShow.size() > 0) {
            mListShow.clear();
        }
        mNotAcceptAdapter.notifyDataSetChanged();
    }

    @OnClick({R.id.btn_date, R.id.btn_clear_zero, R.id.iv_print_label_sub, R.id.iv_allocate_accept_search,
            R.id.iv_print_label_add, R.id.btn_allocate_accept_submit, R.id.btn_allocate_accept_history})
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
            case R.id.iv_print_label_add: {
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                count++;
                mEtPrintLabelCount.setText(String.valueOf(count));
            }
            break;
            case R.id.iv_print_label_sub: {
                int count = Integer.parseInt(mEtPrintLabelCount.getText().toString().trim());
                if (count > 0) {
                    count--;
                    mEtPrintLabelCount.setText(String.valueOf(count));
                }
            }
            break;
            case R.id.iv_allocate_accept_search:
                keySearch();
                break;
            case R.id.btn_allocate_accept_submit:
                if (mAllocateRecord == null) {
                    return;
                }
                if (check()) {
                    return;
                }
                mBtnAllocateAcceptSubmit.setEnabled(false);
                showLoadingDialog(false);
                if (mVideoFragment.isLight()) {
                    mVideoFragment.screenshot(mHandlerShotPic);
                } else {
                    shotResult(null);
                }
                break;
            case R.id.btn_allocate_accept_history:
                showHistory();
                break;
            default:
                break;
        }
    }

    /**
     * 显示历史
     */
    private void showHistory() {
        if (mAllocateAcceptPopWindow == null) {
            mAllocateAcceptPopWindow = new HistoryAllocateAcceptPopWindow(getContext(), mSpWareHouseIn);
            mAllocateAcceptPopWindow.setOnDeleteListener(this);
            mAllocateAcceptPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (hasCancelAccept) {
                        mNotAcceptAdapter.notifyDataSetChanged();
                    }
                    hasCancelAccept = false;
                }
            });
        }
        refreshHistoryList();
    }

    @Override
    public void delete(DbImageUpload data) {
        mDbImageUpload = data;
        requestCancel(data.getStockInUuid());
    }

    /**
     * 请求删除历史
     */
    private void requestCancel(String uuid) {
        showLoadingDialog(false);
        if (!TextUtils.isEmpty(uuid)) {
            cancelAccept(uuid);
        } else {
            onCancelSuccess();
        }
    }

    /**
     * 撤销调拨验收
     */
    private void cancelAccept(String uuid) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("uuid", uuid);
            getIRequestDataService().onGetDataListener(TYPE_ALLOCATE_ACCEPT_CANCEL, params, new OnRequestDataListener.Stub() {
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

    /**
     * 撤销成功
     */
    private void onCancelSuccess() {
        //先刷新后删除数据库
        refreshCancelAccept();
        getDbManager().deleteDbImageUpload(mDbImageUpload);
        refreshHistoryList();
        ToastUtil.showShortToast(getContext(), "撤销成功");
        hasCancelAccept = true;
        dismissLoadingDialog();
        mDbImageUpload = null;
    }

    /**
     * 撤销失败
     */
    public void onCancelFailed(Message message) {
        dismissLoadingDialog();
        mDbImageUpload = null;
        ToastUtil.showShortToast(getContext(), MessageUtil.getObj(message));
    }

    /**
     * 撤销成功后刷新列表数据
     */
    private void refreshCancelAccept() {
        String date = mDbImageUpload.getDate();
        String currentSelectedDate = mBtnDate.getText().toString().trim();
        if (!date.equals(currentSelectedDate)) {
            return;
        }
        StockInRecord stockInRecord = GsonUtil.getGson().fromJson(mDbImageUpload.getLine(), StockInRecord.class);
        String cancelUuid = stockInRecord.getSourceBillLineUuid();
        BigDecimal quantity = stockInRecord.getQuantity();
        AllocateRecord allocateRecord = null;
        for (AllocateRecord data : mListAll) {
            String uuid = data.getUuid();
            if (cancelUuid.equals(uuid)) {
                //已验收
                allocateRecord = data;
                BigDecimal acceptQty = data.getAcceptQty();
                data.setAcceptQty(acceptQty.subtract(quantity));
                break;
            }
        }
        if (allocateRecord == null) {
            return;
        }
        //修改累计
        if (mMapAccumulate.containsKey(cancelUuid)) {
            BigDecimal coefficient = allocateRecord.getWeightCoefficient();
            BigDecimal numBeforeModify = new BigDecimal(mMapAccumulate.get(cancelUuid));
            BigDecimal numAfterModify;
            if (coefficient != null && coefficient.doubleValue() != 0) {
                numAfterModify = numBeforeModify.subtract(quantity.multiply(coefficient));
            } else {
                numAfterModify = numBeforeModify.subtract(quantity);
            }
            String value = BigDecimalUtil.toScaleStr(numAfterModify);
            mMapAccumulate.put(cancelUuid, value);
            mEtWeightAccumulate.setText(value);
        }
    }

    /**
     * 刷新历史列表
     */
    private void refreshHistoryList() {
        //获取数据库当天的数据
        String date = mBtnDate.getText().toString().trim();
        List<DbImageUpload> daoList = getDbManager().getDbListAllocateAcceptHistory(date);
        Collections.reverse(daoList);
        mListHistory.clear();
        mListHistory.addAll(daoList);
        mAllocateAcceptPopWindow.show();
        mAllocateAcceptPopWindow.notify(mListHistory);
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

    /**
     * 显示结果，并将数据保存到后台进行请求
     */
    private void shotResult(String imagePath) {
        StockInRecord record = new StockInRecord();
        record.setGoods(mAllocateRecord.getGoods());
        record.setGoodsUnit(mAllocateRecord.getGoodsUnit());
        record.setTraceCode(mAllocateRecord.getTraceCode());
        //扣重数
        record.setDeductQty(getEtBigDecimal(mEtDeductWeight));
        //扣皮数
        record.setLeatherQty(getEtBigDecimal(mEtBucklesLeather));
        //验收数（入库数）
        record.setQuantity(getEtBigDecimal(mEtNumWarehousing));
        //调拨记录uuid
        record.setSourceBillLineUuid(mAllocateRecord.getUuid());

        String date = mBtnDate.getText().toString().trim();
        DbImageUpload db = new DbImageUpload();
        db.setDate(date);
        db.setOperatime(DateUtils.getTime2(new Date()));
        db.setImagePath(imagePath);
        db.setLine(GsonUtil.getGson().toJson(record));
        db.setType(Common.DbType.TYPE_ALLOCATE_ACCEPT);
        getDbManager().insertDbImageUpload(db);
        ToastUtil.showShortToast(getContext(), "越库调拨成功");

        refreshSuccessData(record);
        mBtnAllocateAcceptSubmit.setEnabled(true);
        dismissLoadingDialog();
    }

    /**
     * 调拨验收请求成功后刷新数据
     */
    private void refreshSuccessData(StockInRecord record) {
        // 保存累计重量
        if (mAllocateRecord != null) {
            //打印标签
            int printCount = Integer.parseInt(mEtPrintLabelCount.getText().toString());
            if (printCount > 0) {
                String name = mAllocateRecord.getGoods().getName();
                String code = mAllocateRecord.getTraceCode();
                PrinterBean printerBean = PrinterBean.get(printCount, code, null, null, name, null, null, null);
                PrinterInventory.printer(getContext(), printerBean);
            }
            //转化为kg
            BigDecimal weightCoefficient = mAllocateRecord.getWeightCoefficient();
            BigDecimal numWarehousing = getEtBigDecimal(mEtNumWarehousing);
            //设置累计
            BigDecimal accumulate = getEtBigDecimal(mEtWeightAccumulate);
            if (weightCoefficient != null) {
                mMapAccumulate.put(mAllocateRecord.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing.multiply(weightCoefficient))));
            } else {
                mMapAccumulate.put(mAllocateRecord.getUuid(), BigDecimalUtil.toScaleStr(accumulate.add(numWarehousing)));
            }
            //保存已验收数
            mAllocateRecord.setAcceptQty(mAllocateRecord.getAcceptQty().add(record.getQuantity()));
            clearContent();
            mNotAcceptAdapter.notifyDataSetChanged();
            mAllocateRecord = null;
            //光标移动到采购标签
            mEtInventoryLabel.requestFocus();
        }
    }

    private void clearContent() {
        //清除关键字
        mEtKeySearch.setText("");
        //清除文本
        clearText();
    }

    private boolean check() {
        if (Integer.parseInt(mEtPrintLabelCount.getText().toString().trim()) < 0) {
            ToastUtil.showShortToast(getContext(), "标签数不能小于0");
            return true;
        }
        if (TextUtils.isEmpty(mEtNumWarehousing.getText().toString().trim())) {
            ToastUtil.showShortToast(getContext(), "入库数量不能为空");
            return true;
        }
        return false;
    }

    /**
     * 文本框监听
     */
    private onInputFinishListener mOnInputFinishListenerSetStoreInNum = new onInputFinishListener() {
        @Override
        public void onFinish(String key) {
            if (mAllocateRecord == null) {
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
        BigDecimal weightCoefficient = mAllocateRecord.getWeightCoefficient();
        BigDecimal finallyCount;
        if (weightCoefficient != null) {
            finallyCount = amount.subtract(leather).subtract(deduct).divide(weightCoefficient, ROUND_HALF_EVEN);
        } else {
            finallyCount = amount;
        }
        if (finallyCount.doubleValue() < 0) {
            mEtNumWarehousing.setText("0.00");
        } else {
            String num = BigDecimalUtil.toScaleStr(finallyCount);
            mEtNumWarehousing.setText(num);
        }
    }

    private onScanFinishListener mLabelOnScanFinishListener = new onScanFinishListener() {
        @Override
        public void onScanFinish(String key) {
            if (!TextUtils.isEmpty(key)) {
                scanLabel(key);
            }
        }
    };

    private void scanLabel(String key) {
        showLoadingDialog(false);
        mEtInventoryLabel.setText("");
        KeyBoardUtils.closeKeybord(mEtInventoryLabel, getContext());
        if (mListAll.size() == 0) {
            ToastUtil.showShortToast(getContext(), "暂无数据");
            dismissLoadingDialog();
            return;
        }
        boolean hasGoods = false;
        for (AllocateRecord data : mListAll) {
            String traceCode = data.getTraceCode();
            if (key.equals(traceCode)) {
                hasGoods = true;
                mAllocateRecord = data;
                setAllocateRecordInfo();
                dismissLoadingDialog();
                break;
            }
        }
        if (!hasGoods) {
            ToastUtil.showShortToast(getContext(), "暂无数据");
        }
        dismissLoadingDialog();
    }

    /**
     * 日期选择监听
     */
    private DatePickerDialog.OnDateSetListener mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
            String date = year + "-" + (month + 1) + "-" + dayOfMonth;
            mBtnDate.setText(date);
            Warehouse selectedItem = mSpWareHouseIn.getSelectedItem();
            if (selectedItem != null) {
                //请求数据
                clearGoodsList();
                showLoadingDialog(false);
                queryNotAcceptData(selectedItem.getUuid());
            } else {
                ToastUtil.showShortToast(getContext(), "无仓库数据");
            }
        }
    };

    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mAllocateRecord = mListShow.get(position);
            setAllocateRecordInfo();
        }
    };

    private void setAllocateRecordInfo() {
        clearText();
        //设置累计重量
        if (mMapAccumulate.containsKey(mAllocateRecord.getUuid())) {
            mEtWeightAccumulate.setText(mMapAccumulate.get(mAllocateRecord.getUuid()));
        } else {
            mEtWeightAccumulate.setText("");
        }
        BigDecimal weightCoefficient = mAllocateRecord.getWeightCoefficient();
        if (weightCoefficient != null) {
            mTvWeightAccumulateUnit.setText("kg");
            mEtBucklesLeather.setEnabled(true);
            mEtDeductWeight.setEnabled(true);
            mEtNumWarehousing.setEnabled(false);
            mIvAdd.setVisibility(View.GONE);
            mIvSub.setVisibility(View.GONE);
        } else {
            mTvWeightAccumulateUnit.setText(mAllocateRecord.getGoodsUnit().getName());
            mEtBucklesLeather.setEnabled(false);
            mEtDeductWeight.setEnabled(false);
            mEtNumWarehousing.setEnabled(true);
            mIvAdd.setVisibility(View.VISIBLE);
            mIvSub.setVisibility(View.VISIBLE);
        }
        //商品名
        UCN goods = mAllocateRecord.getGoods();
        setText(mTvInfoGood, (goods != null && !TextUtils.isEmpty(goods.getName())) ? goods.getName() : "");
        //待入库数
        IdName goodsUnit = mAllocateRecord.getGoodsUnit();
        String unit = (goodsUnit != null && !TextUtils.isEmpty(goodsUnit.getName()) ? goodsUnit.getName() : "");
        BigDecimal allocateQty = mAllocateRecord.getAllocateQty();
        String strAllocateQty = BigDecimalUtil.toScaleStr(allocateQty);
        setText(mTvInfoNum, strAllocateQty + (unit));
        //调拨备注
        setText(mTvInfoRemark, "调拨备注：" + (mAllocateRecord.getRemark() == null ? "" : mAllocateRecord.getRemark()));
        //设置入库数量单位
        mTvNumWarehousingUnit.setText(unit);
        // 入库数量  (入库数量默认等于采购数量)
        String currentWeight = mEtWeightCurrent.getText().toString().trim();
        if (mAllocateRecord.getWeightCoefficient() != null && !TextUtils.isEmpty(currentWeight)) {
            // 设置为当前体重秤上面的数值
            setEditText(mEtNumWarehousing, currentWeight);
        } else {
            setEditText(mEtNumWarehousing, strAllocateQty);
        }
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
        mTvInfoNum.setText("");
        mTvInfoRemark.setText("调拨备注：");
        mTvNumWarehousingUnit.setText("kg");
        setEtWeightCurrent("");
        mEtWeightAccumulate.setText("");
        mTvWeightAccumulateUnit.setText("kg");
        mEtBucklesLeather.setText("");
        mEtDeductWeight.setText("");
        mEtNumWarehousing.setText("");
    }

    private void setEtWeightCurrent(String weight) {
        mEtWeightCurrent.setText(weight);
        mPreWeight = weight;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_ware_house_in:
                showLoadingDialog(false);
                clearGoodsList();
                Warehouse selectedItem = mSpWareHouseIn.getSelectedItem();
                queryNotAcceptData(selectedItem.getUuid());
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private View getEditText(int viewGroupId, String title) {
        View viewGroup = findViewById(viewGroupId);
        ((TextView) viewGroup.findViewById(R.id.tv_title)).setText(title);
        return viewGroup.findViewById(R.id.ed_content);
    }

    private class NotAcceptAdapter extends CommonAdapter4Lv<AllocateRecord> {

        private NotAcceptAdapter(Context context, List<AllocateRecord> data) {
            super(context, R.layout.item_line, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, AllocateRecord record, int position) {
            //商品名称
            String name = record.getGoods().getName();
            // 待验收数
            BigDecimal acceptingQty = record.getAllocateQty();
            // 已验收数
            BigDecimal acceptedQty = record.getAcceptQty();
            //单位
            String unit = record.getGoodsUnit().getName();
            holder.setText(R.id.tv_goods_name, name)
                    .setText(R.id.tv_goods_purchase, "待验收数:" + BigDecimalUtil.toScaleStr(acceptingQty) + unit)
                    .setText(R.id.tv_goods_sort_out, "已验收数:" + BigDecimalUtil.toScaleStr(acceptedQty) + unit);
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
                    if (mAllocateRecord.getWeightCoefficient().doubleValue() != 0) {
                        defaultWeightCoefficient = mAllocateRecord.getWeightCoefficient();
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

    /**
     * 当前选择为数量单位
     */
    private boolean isCount() {
        return mAllocateRecord != null && mAllocateRecord.getWeightCoefficient() == null;
    }

    /**
     * 当前选择为重量单位
     */
    private boolean isWeight() {
        return mAllocateRecord != null
                && mAllocateRecord.getWeightCoefficient() != null
                && mAllocateRecord.getWeightCoefficient().doubleValue() != 0;
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

    private BigDecimal getEtBigDecimal(EditText et) {
        String str = et.getText().toString().trim();
        return new BigDecimal(!TextUtils.isEmpty(str) ? str : "0");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbRefreshUtil.refreshUnRegist(this);
    }

}
