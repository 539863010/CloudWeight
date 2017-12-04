package xm.cloudweight.widget;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import xm.cloudweight.widget.impl.onScanFinishListener;

/**
 * @author wyh
 * @Description: 针对扫描枪多次扫描内容叠加bug
 * @creat 2017/11/9
 */
public class ScanEditText extends android.support.v7.widget.AppCompatEditText {

    private onScanFinishListener mListener;
    private Handler mHandlerSearch = new Handler();
    /**
     * 延迟线程，看是否还有下一个字符输入
     */
    private Runnable mRunnableSearch = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onScanFinish(mSubstring);
                //显示最新一次 扫描的内容
                removeTextChangedListener(textWatcher);
                setText(mSubstring);
                setSelection(mSubstring.length());
                addTextChangedListener(textWatcher);
            }
        }
    };
    private String mSubstring;

    public ScanEditText(Context context) {
        super(context);
        init();
    }

    public ScanEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String string = s.toString();
            if (!string.isEmpty()) {
                if (count > before) {
                    //获取最新一次扫描出的内容
                    mSubstring = string.substring(before, string.length());
                } else {
                    //删除的时候不处理
                    mSubstring = string;
                }
            } else {
                mSubstring = "";
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            mHandlerSearch.removeCallbacks(mRunnableSearch);
            mHandlerSearch.postDelayed(mRunnableSearch, 500);
        }
    };

    public void setOnScanFinishListener(onScanFinishListener listener) {
        mListener = listener;
    }

}
