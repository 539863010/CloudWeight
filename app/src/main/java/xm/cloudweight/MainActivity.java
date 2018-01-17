package xm.cloudweight;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.xmzynt.storm.service.user.merchant.Merchant;
import com.xmzynt.storm.util.GsonUtil;

import butterknife.OnClick;
import xm.cloudweight.base.BaseActivity;
import xm.cloudweight.comm.Common;
import xm.cloudweight.utils.bussiness.LocalSpUtil;
import xm.cloudweight.utils.bussiness.RefreshMerchantHelper;

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
        // 发送广播更新远程服务里面Merchant
        Merchant merchant = LocalSpUtil.getMerchant(this);
        RefreshMerchantHelper.send(this, GsonUtil.getGson().toJson(merchant));
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
        RefreshMerchantHelper.send(this, "");

        Context ctx = getContext();
        LocalSpUtil.setMerchant(ctx, "");
        LocalSpUtil.setCookie(ctx, "");
        LocalSpUtil.setListCustomerLevel(ctx, null);
        LocalSpUtil.setListWareHouse(ctx, null);
        startActivity(LoginActivity.class);
        finish();
    }

    @Override
    public String getBaseTitle() {
        return getResources().getString(R.string.lbhkzs);
    }
}
