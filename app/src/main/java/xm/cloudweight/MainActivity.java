package xm.cloudweight;

import android.content.Intent;
import android.view.View;

import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.bussiness.LocalSpUtil;

/**
 * @author wyh
 * @Description: 主界面
 * @create 2017/10/25
 */
public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initContentView() {
    }

    @Override
    protected void loadDate() {

    }

    @OnClick({R.id.btn_accepting, R.id.btn_sorting, R.id.btn_stockout, R.id.btn_allocating, R.id.btn_checking, R.id.btn_logout})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_accepting:
                //验收
                startActivity(CheckInActivity.class);
                break;
            case R.id.btn_sorting:
                //分拣
                startActivity(SortOutActivity.class);
                break;
            case R.id.btn_stockout:
                //出库
                goSimilarActivity(Common.SIMILAR_STOCKOUT);
                break;
            case R.id.btn_allocating:
                //调拨
                goSimilarActivity(Common.SIMILAR_ALLOCATE);
                break;
            case R.id.btn_checking:
                //盘点
                goSimilarActivity(Common.SIMILAR_CHECK);
                break;
            case R.id.btn_logout:
                //退出
                requestLogout();
                break;
            default:
                break;
        }
    }

    private void goSimilarActivity(int type) {
        Intent intent = new Intent(this, SimilarActivity.class);
        intent.putExtra(SimilarActivity.KEY_TYPE, type);
        startActivity(intent);
    }

    /**
     * 退出登录
     */
    private void requestLogout() {
        LocalSpUtil.setMerchant(getContext(), "");
        LocalSpUtil.setCookie(getContext(), "");
        startActivity(LoginActivity.class);
        finish();
    }
    @Override
    public String getBaseTitle() {
        return getResources().getString(R.string.lbhkzs);
    }
}