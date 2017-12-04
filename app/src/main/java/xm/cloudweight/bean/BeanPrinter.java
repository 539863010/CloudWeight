package xm.cloudweight.bean;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/30
 */
public class BeanPrinter {

    // 商品名
   private String goodsName;
    // 数量
    private String count;
    //顾客
    private String customer;
    // 部门
    private String department;
    //追溯码
    private String code;

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
