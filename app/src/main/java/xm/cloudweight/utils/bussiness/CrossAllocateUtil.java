package xm.cloudweight.utils.bussiness;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xmzynt.storm.service.wms.warehouse.Warehouse;

import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.bean.SaveWareHouse;
import xm.cloudweight.widget.CommonAdapter4Lv;
import xm.cloudweight.widget.CommonHolder4Lv;

/**
 * @author wyh
 * @Description:
 * @creat 2017/12/26
 */
public class CrossAllocateUtil {

    /**
     * spinner不能搭配popwindow ，只能用AlertDialog
     */

    public static AlertDialog create(Context context, final onCrossAllocateOperationInterface onCrossAllocateOperationInterface) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialog);
        View v = LayoutInflater.from(context).inflate(R.layout.pop_cross_allocate, null);
        builder.setView(v);
        builder.setCancelable(false);

        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(context);
        final ListView lv = v.findViewById(R.id.lv_warehouse);
        lv.setVisibility(View.INVISIBLE);
        final TextView tvCrossAllocate = v.findViewById(R.id.tv_corss_allocate);
        tvCrossAllocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lv.getVisibility() == View.INVISIBLE) {
                    lv.setVisibility(View.VISIBLE);
                } else {
                    lv.setVisibility(View.INVISIBLE);
                }
            }
        });
        final CommonAdapter4Lv<Warehouse> commonAdapter4Lv = new CommonAdapter4Lv<Warehouse>(context, R.layout.item_cross_allocate, listWareHouse) {
            @Override
            public void doSomething(CommonHolder4Lv holder, Warehouse warehouse, int position) {
                holder.setText(R.id.tv_show, warehouse.getName());
            }
        };
        final SaveWareHouse saveWareHouse = new SaveWareHouse();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Warehouse warehouse = commonAdapter4Lv.getT(position);
                saveWareHouse.setWareHouse(warehouse);
                tvCrossAllocate.setText(warehouse.getName());
                lv.setVisibility(View.INVISIBLE);
            }
        });
        if (listWareHouse != null) {
            if (listWareHouse.size() > 0) {
                Warehouse warehouse = listWareHouse.get(0);
                tvCrossAllocate.setText(warehouse.getName());
                saveWareHouse.setWareHouse(warehouse);
            }
            lv.setAdapter(commonAdapter4Lv);
        }
        v.findViewById(R.id.btn_cross_allocate_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCrossAllocateOperationInterface != null) {
                    onCrossAllocateOperationInterface.cancel();
                }
            }
        });
        v.findViewById(R.id.btn_cross_allocate_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onCrossAllocateOperationInterface != null) {
                    onCrossAllocateOperationInterface.sure(saveWareHouse.getWareHouse());
                }
            }
        });
        return builder.create();
    }

    public static void show(AlertDialog alertDialog) {
        if (alertDialog == null) {
            return;
        }
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    public interface onCrossAllocateOperationInterface {

        void cancel();

        void sure(Warehouse warehouse);

    }

}
