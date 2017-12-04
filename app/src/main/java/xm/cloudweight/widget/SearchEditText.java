package xm.cloudweight.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.util.AttributeSet;

import xm.cloudweight.widget.impl.onInputFinishListener;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/9
 */
public class SearchEditText extends android.support.v7.widget.AppCompatEditText {

    private onInputFinishListener mListener;
    private Handler mHandlerSearch = new Handler();
    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable mRunnableSearch = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onFinish(getText().toString().trim());
            }
        }
    };

    public SearchEditText(Context context) {
        super(context);
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mHandlerSearch.removeCallbacks(mRunnableSearch);
                mHandlerSearch.postDelayed(mRunnableSearch, 500);
            }
        });
    }

    public void setOnInputFinishListener(onInputFinishListener listener) {
        mListener = listener;
    }

}
