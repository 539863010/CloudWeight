package xm.cloudweight.utils.bussiness.printer;

/**
 * @author : wyh
 * @create : 2018/2/28
 * @des :  打印bean
 */
public class PrinterBean {

    /**
     * 打印标签数量
     */
    private int printCount;
    /**
     * 打印码
     * 1.分拣标签是PlatformTraceCode
     * 2.库存标签用TraceCode 或采购的purchaseBatch
     */
    private String code;
    /**
     * 客户
     */
    private String customer;
    /**
     * 部门
     */
    private String department;
    /**
     * 商品名
     */
    private String goodsName;
    /**
     * 数量
     */
    private String num;
    /**
     * 保存条件
     */
    private String storageMode;
    /**
     * 保质期
     */
    private String period;

    public static PrinterBean get(int count, String code, String customer, String department, String goodsName, String num, String storageMode, String period) {
        PrinterBean bean = new PrinterBean();
        bean.setPrintCount(count);
        bean.setCode(code);
        bean.setCustomer(customer);
        bean.setDepartment(department);
        bean.setGoodsName(goodsName);
        bean.setNum(num);
        bean.setStorageMode(storageMode);
        bean.setPeriod(period);
        return bean;
    }

    public int getPrintCount() {
        return printCount;
    }

    public void setPrintCount(int printCount) {
        this.printCount = printCount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(String storageMode) {
        this.storageMode = storageMode;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}
