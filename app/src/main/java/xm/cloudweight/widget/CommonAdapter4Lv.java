package xm.cloudweight.widget;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * @author wyh
 * @Description: ListView通用适配器
 * @creat 2017/2/27
 */
public abstract class CommonAdapter4Lv<T> extends BaseAdapter {

    public final Context mContext;
    public List<T> mData;
    private SparseIntArray mType2LayoutId;
    private final int DEFAULT_TYPE = 999;

    public CommonAdapter4Lv(Context context, int layoutId, List<T> data) {
        this.mContext = context;
        this.mData = data;

        SparseIntArray map = new SparseIntArray();
        map.put(DEFAULT_TYPE, layoutId);
        this.mType2LayoutId = map;
    }

    public CommonAdapter4Lv(Context context, SparseIntArray map, List<T> datas) {
        this.mContext = context;
        this.mData = datas;
        this.mType2LayoutId = map;
    }

    @Override
    public View getView(int position, View converView, ViewGroup viewGroup) {
        CommonHolder4Lv holder = CommonHolder4Lv.get(mContext, mType2LayoutId.get(getItemViewType(position)), position, converView, viewGroup);
        doSomething(holder, getT(position), position);
        return holder.getConvertView();
    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public List<T> getData() {
        return mData;
    }

    public abstract void doSomething(CommonHolder4Lv holder, T t, int position);

    @Override
    public int getItemViewType(int position) {
        return DEFAULT_TYPE;
    }

    public T getT(int position) {
        return mData == null || mData.size() == 0 ? null : mData.get(position);
    }

}
