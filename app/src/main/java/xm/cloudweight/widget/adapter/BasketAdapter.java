package xm.cloudweight.widget.adapter;

import android.content.Context;
import android.view.View;

import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;

/**
 * 周转筐Adapter
 */
public class BasketAdapter extends CommonAdapter4Lv<String> {

    private OnBasketRemoveListener mRemoveListener;

    public BasketAdapter(Context context, List<String> data) {
        super(context, R.layout.item_basket, data);
    }

    @Override
    public void doSomething(final CommonHolder4Lv holder, final String backetNum, final int position) {
        holder.setText(R.id.tv_basket, backetNum)
                .setOnClickListener(R.id.btn_delete, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mRemoveListener != null) {
                            mRemoveListener.onRemove(backetNum, position);
                        }
                    }
                });
    }

    public void setOnBasketRemoveListener(OnBasketRemoveListener removeListener) {
        mRemoveListener = removeListener;
    }

    public interface OnBasketRemoveListener {

        void onRemove(String basketNum, int position);

    }

}