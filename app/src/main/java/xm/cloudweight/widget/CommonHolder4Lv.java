package xm.cloudweight.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author wyh
 * @Description: ListView通用ViewHolder
 * @creat 2017/2/27
 */
public class CommonHolder4Lv {

    private final SparseArray<View> mViews;
    private final Context mContext;
    private int mPosition;
    private View mItemView;
    private int mLayoutId;

    public CommonHolder4Lv(Context context, int position, View itemView, ViewGroup parent) {
        this.mContext = context;
        this.mViews = new SparseArray<>();
        this.mPosition = position;
        this.mItemView = itemView;
        mItemView.setTag(this);
    }

    public static CommonHolder4Lv get(Context context, int layoutId, int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            View itemView = LayoutInflater.from(context).inflate(layoutId, parent,
                    false);
            CommonHolder4Lv holder = new CommonHolder4Lv(context, position, itemView, parent);
            holder.mLayoutId = layoutId;
            return holder;
        } else {
            CommonHolder4Lv holder = (CommonHolder4Lv) convertView.getTag();
            holder.mPosition = position;
            return holder;
        }
    }

    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (null == view) {
            view = mItemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public CommonHolder4Lv setText(int viewId, String text) {
        TextView tv = getView(viewId);
        if (TextUtils.isEmpty(text)) {
            tv.setText("");
        } else {
            tv.setText(text);
        }
        return this;
    }

    public CommonHolder4Lv setText(int viewId, Spanned text) {
        TextView tv = getView(viewId);
        tv.setText(text);
        return this;
    }

    public CommonHolder4Lv setImageByUrl(int viewId, String url) {
        // TODO: 2017/2/24
        return this;
    }

    public CommonHolder4Lv setImageResource(int viewId, int resId) {
        ImageView view = getView(viewId);
        view.setImageResource(resId);
        return this;
    }

    public CommonHolder4Lv setImageIcon(int viewId, int iconType, String id) {
//        GetImageUtils.getIcon(mContext, iconType, id,
//                (ImageView) getView(viewId));
        return this;
    }

    public CommonHolder4Lv setImageIcon4Hospital(int viewId, int iconType, String id) {
//        GetImageUtils.getIconHospital(mContext, iconType, id,
//                (ImageView) getView(viewId));
        return this;
    }

    public CommonHolder4Lv setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    public CommonHolder4Lv setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = getView(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    public CommonHolder4Lv setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public CommonHolder4Lv setBackgroundRes(int viewId, int backgroundRes) {
        View view = getView(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    public CommonHolder4Lv setTextColor(int viewId, int textColor) {
        TextView view = getView(viewId);
        view.setTextColor(textColor);
        return this;
    }

    public CommonHolder4Lv setTextColorRes(int viewId, int textColorRes) {
        TextView view = getView(viewId);
        view.setTextColor(ContextCompat.getColor(mContext, textColorRes));
        return this;
    }

    @SuppressLint("NewApi")
    public CommonHolder4Lv setAlpha(int viewId, float value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getView(viewId).setAlpha(value);
        } else {
            // Pre-honeycomb hack to set Alpha value
            AlphaAnimation alpha = new AlphaAnimation(value, value);
            alpha.setDuration(0);
            alpha.setFillAfter(true);
            getView(viewId).startAnimation(alpha);
        }
        return this;
    }

    public CommonHolder4Lv setVisible(int viewId, int visible) {
        View view = getView(viewId);
        if (view.getVisibility() != visible) {
            view.setVisibility(visible);
        }
        return this;
    }

    public CommonHolder4Lv setTag(int viewId, int key, Object tag) {
        View view = getView(viewId);
        view.setTag(key, tag);
        return this;
    }

    public CommonHolder4Lv setSelected(int viewId, boolean selected) {
        View view = getView(viewId);
        view.setSelected(selected);
        return this;
    }

    public CommonHolder4Lv setChecked(int viewId, boolean checked) {
        Checkable view = (Checkable) getView(viewId);
        view.setChecked(checked);
        return this;
    }

    public CommonHolder4Lv setPadding(int viewId, int l, int t, int r, int b) {
        View view = getView(viewId);
        view.setPadding(l, t, r, b);
        return this;
    }

    /**
     * 关于事件的
     */
    public CommonHolder4Lv setOnClickListener(int viewId,
                                              View.OnClickListener listener) {
        View view = getView(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    public CommonHolder4Lv setOnTouchListener(int viewId,
                                              View.OnTouchListener listener) {
        View view = getView(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    public CommonHolder4Lv setOnLongClickListener(int viewId,
                                                  View.OnLongClickListener listener) {
        View view = getView(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

    public View getConvertView() {
        return mItemView;
    }

    public int getPosition() {
        return mPosition;
    }

    public int getLayoutId() {
        return mLayoutId;
    }

}
