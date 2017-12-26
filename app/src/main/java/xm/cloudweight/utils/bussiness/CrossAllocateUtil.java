package xm.cloudweight.utils.bussiness;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.xmzynt.storm.service.wms.warehouse.Warehouse;

import java.util.List;

import xm.cloudweight.R;
import xm.cloudweight.widget.DataSpinner;

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
        final DataSpinner<Warehouse> spCrossAllocate = v.findViewById(R.id.sp_corss_allocate_warehouse_in);
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
                    onCrossAllocateOperationInterface.sure(spCrossAllocate.getSelectedItem());
                }
            }
        });
        List<Warehouse> listWareHouse = LocalSpUtil.getListWareHouse(context);
        if (listWareHouse != null) {
            spCrossAllocate.setList(listWareHouse);
        }
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
