package xm.cloudweight.utils.dao.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

/**
 * @author wyh
 * @Description:
 * @creat 2017/11/9
 */
@Entity
public class DbImageUpload {

    @Id(autoincrement = true)
    private Long id;
    @Property(nameInDb = "StockOutUuid")
    private String stockOutUuid;
    @Property(nameInDb = "StockInUuid")
    private String stockInUuid;
    @Property(nameInDb = "isRequestSuccess")
    private boolean isRequestSuccess;
    @Property(nameInDb = "IsUploadStockOutImage")
    private boolean isUploadStockOutImage;
    @Property(nameInDb = "IsUploadStockInImage")
    private boolean isUploadStockInImage;
    @Property(nameInDb = "ImageUrl")
    private String imageUrl;
    @Property(nameInDb = "ImagePath")
    private String imagePath;
    @Property(nameInDb = "LineData")
    private String line;
    @Property(nameInDb = "Date")
    private String date;
    @Property(nameInDb = "OperaTime")
    private String operatime;
    @Property(nameInDb = "Type")    //保存到数据库的类型
    private int type;
    @Property(nameInDb = "ErrorType")    //错误类型
    private int errorType;
    @Property(nameInDb = "ErrorString")    //错误内容
    private String errorString;

    @Generated(hash = 190316064)
    public DbImageUpload(Long id, String stockOutUuid, String stockInUuid,
                         boolean isRequestSuccess, boolean isUploadStockOutImage,
                         boolean isUploadStockInImage, String imageUrl, String imagePath,
                         String line, String date, String operatime, int type, int errorType,
                         String errorString) {
        this.id = id;
        this.stockOutUuid = stockOutUuid;
        this.stockInUuid = stockInUuid;
        this.isRequestSuccess = isRequestSuccess;
        this.isUploadStockOutImage = isUploadStockOutImage;
        this.isUploadStockInImage = isUploadStockInImage;
        this.imageUrl = imageUrl;
        this.imagePath = imagePath;
        this.line = line;
        this.date = date;
        this.operatime = operatime;
        this.type = type;
        this.errorType = errorType;
        this.errorString = errorString;
    }

    @Generated(hash = 1112138487)
    public DbImageUpload() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockOutUuid() {
        return this.stockOutUuid;
    }

    public void setStockOutUuid(String stockOutUuid) {
        this.stockOutUuid = stockOutUuid;
    }

    public String getStockInUuid() {
        return this.stockInUuid;
    }

    public void setStockInUuid(String stockInUuid) {
        this.stockInUuid = stockInUuid;
    }

    public boolean getIsRequestSuccess() {
        return this.isRequestSuccess;
    }

    public void setIsRequestSuccess(boolean isRequestSuccess) {
        this.isRequestSuccess = isRequestSuccess;
    }

    public boolean getIsUploadStockOutImage() {
        return this.isUploadStockOutImage;
    }

    public void setIsUploadStockOutImage(boolean isUploadStockOutImage) {
        this.isUploadStockOutImage = isUploadStockOutImage;
    }

    public boolean getIsUploadStockInImage() {
        return this.isUploadStockInImage;
    }

    public void setIsUploadStockInImage(boolean isUploadStockInImage) {
        this.isUploadStockInImage = isUploadStockInImage;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getLine() {
        return this.line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOperatime() {
        return this.operatime;
    }

    public void setOperatime(String operatime) {
        this.operatime = operatime;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getErrorType() {
        return this.errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public String getErrorString() {
        return this.errorString;
    }

    public void setErrorString(String errorString) {
        this.errorString = errorString;
    }

}
