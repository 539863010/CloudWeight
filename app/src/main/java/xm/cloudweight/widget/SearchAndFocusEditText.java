package xm.cloudweight.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;

import xm.cloudweight.widget.impl.onInputFinishListener;

/**
 * @author wyh
 * @Description: 监听输入完成  ，  防止多个EditText相互监听， 出现死循环
 * @creat 2017/11/9
 */
public class SearchAndFocusEditText extends android.support.v7.widget.AppCompatEditText {

    private ArrayList<TextWatcher> mListeners;
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
    private BaseTextWatcher mBaseTextWatcher = new BaseTextWatcher() {
        @Override
        public void afterTextChanged(Editable editable) {
            mHandlerSearch.removeCallbacks(mRunnableSearch);
            mHandlerSearch.postDelayed(mRunnableSearch, 500);
        }
    };

    public SearchAndFocusEditText(Context context) {
        super(context);
    }

    public SearchAndFocusEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchAndFocusEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (mListener != null) {
            if (focused) {
                addTextChangedListener(mBaseTextWatcher);
            } else {
                removeTextChangedListener(mBaseTextWatcher);
            }
        }
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(watcher);

        super.addTextChangedListener(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mListeners != null) {
            int i = mListeners.indexOf(watcher);
            if (i >= 0) {
                mListeners.remove(i);
            }
        }

        super.removeTextChangedListener(watcher);
    }

    public void clearTextChangedListeners() {
        if (mListeners != null) {
            for (TextWatcher watcher : mListeners) {
                super.removeTextChangedListener(watcher);
            }
            mListeners.clear();
            mListeners = null;
        }
    }

    public void setOnInputFinishListener(onInputFinishListener listener) {
        mListener = listener;
    }

}
