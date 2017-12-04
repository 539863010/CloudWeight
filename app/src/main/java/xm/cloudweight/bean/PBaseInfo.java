package xm.cloudweight.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wyh
 * @Description: 接口上传基类
 * @creat 2017/10/30
 */
public class PBaseInfo {

    /**
     * platform : scale
     * userIdentity : merchant
     * sessionId : XM1746HUI1
     * userUuid : 1245522
     * userName : 测试
     */

    private String platform;
    private String userIdentity;
    private String sessionId;
    private String userUuid;
    private String userName;
    private Map<String,Object> body = new HashMap<>();

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserUuid() {
        return userUuid;
    }

    public void setUserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }
}
