package xm.cloudweight.utils.dao.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * @author wyh
 * @Description:
 * @creat 2018/1/18
 */
@Entity
public class DbRequestData {

    @Id(autoincrement = true)
    private Long id;

    @Property(nameInDb = "Data")    //服务器返回的数据
    private String data;

    @Generated(hash = 1164547889)
    public DbRequestData(Long id, String data) {
        this.id = id;
        this.data = data;
    }

    @Generated(hash = 98126727)
    public DbRequestData() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getData() {
        return this.data;
    }

    public void setData(String data) {
        this.data = data;
    }

}
