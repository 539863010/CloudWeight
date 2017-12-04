package xm.cloudweight.widget;

import android.content.Context;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

public class ScalableTextView extends android.support.v7.widget.AppCompatTextView {
    float defaultTextSize = 0.0f;

    public ScalableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
        defaultTextSize = getTextSize();
    }

    public ScalableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
        defaultTextSize = getTextSize();
    }

    public ScalableTextView(Context context) {
        super(context);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.END);
        defaultTextSize = getTextSize();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (layout != null) {
            final int lineCount = layout.getLineCount();
            if (lineCount > 0) {
                int ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                while (ellipsisCount > 0) {
                    final float textSize = getTextSize();

                    // textSize is already expressed in pixels
                    setTextSize(TypedValue.COMPLEX_UNIT_PX, (textSize - 1));

                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    ellipsisCount = layout.getEllipsisCount(lineCount - 1);
                }
            }
        }
    }
}