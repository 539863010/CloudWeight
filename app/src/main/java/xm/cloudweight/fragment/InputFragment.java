package xm.cloudweight.fragment;

import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.base.BaseFragment;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;

/**
 * @author wyh
 * @description: 数字键盘
 * @create 2018/1/8
 */
public class InputFragment extends BaseFragment {

    public static final List<String> mList = Arrays.asList("1", "2", "3", "4", "5",
            "6", "7", "8", "9", ".", "0", "删除", "清空", " ", "确认");
    private GridView mGvInput;
    private EditText mEtClickText;

    public static InputFragment newInstance() {
        return new InputFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_input;
    }

    @Override
    protected void getData() {

    }

    @Override
    protected void initView() {
        mGvInput = mView.findViewById(R.id.gv_input);
        mGvInput.setAdapter(new InputAdapter(getActivity(), R.layout.item_input, mList));
        mGvInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mEtClickText == null) {
                    return;
                }
                String content = mList.get(position);
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                String text = mEtClickText.getText().toString().trim();
                //删除
                if (content.equals("删除")) {
                    if (!TextUtils.isEmpty(text)) {
                        String sub = text.substring(0, text.length() - 1);
                        mEtClickText.setText(sub);
                    }
                } else if (content.equals("清空")) {
                    if (!TextUtils.isEmpty(text)) {
                        mEtClickText.setText("");
                    }
                } else if (content.equals("确认")) {
                    //隐藏
                    hide();
                } else if (content.equals(".")) {
                    if (TextUtils.isEmpty(text)) {
                        mEtClickText.setText("0.");
                    } else {
                        if (!text.contains(".")) {
                            mEtClickText.setText(text.concat("."));
                        }
                    }
                } else {
                    mEtClickText.setText(text.concat(content));
                }
            }
        });
    }

    public void hide() {
        if (isAdded() && isVisible()) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .hide(InputFragment.this)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    protected void loadData() {

    }

    public void setEditTexts(EditText... ets) {
        for (EditText editText : ets) {
            if (editText != null) {
                editText.setInputType(InputType.TYPE_NULL);
                editText.setOnClickListener(mOnClickListener);
                editText.setOnFocusChangeListener(mOnFocusChangeListener);
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            show((EditText) v);
        }
    };

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                return;
            }
            show((EditText) v);
        }
    };

    private void show(EditText v) {
        mEtClickText = v;
        if (!isVisible()) {
            getActivity().getSupportFragmentManager().beginTransaction().show(InputFragment.this)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commitAllowingStateLoss();
        }
    }

    private class InputAdapter extends CommonAdapter4Lv<String> {

        private InputAdapter(Context context, int layoutId, List<String> data) {
            super(context, layoutId, data);
        }

        @Override
        public void doSomething(CommonHolder4Lv holder, String s, int position) {
            TextView tvNum = holder.getView(R.id.tv_num);
            if (s.equals("删除")) {
                holder.setVisible(R.id.iv_back_delete, View.VISIBLE);
                tvNum.setVisibility(View.GONE);
            } else {
                holder.setVisible(R.id.iv_back_delete, View.GONE);
                tvNum.setVisibility(View.VISIBLE);
            }
            if (s.equals("清空") || s.equals("确认")) {
                tvNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen._12sp));
            } else {
                tvNum.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimensionPixelSize(R.dimen._20sp));
            }
            holder.setText(R.id.tv_num, s);
        }
    }

}
