package xm.cloudweight.bean;

/**
 * @author wyh
 * @Description: 登录接口请求体
 * @creat 2017/10/25
 */
public class PLogin {

    /**
     * platform : scale
     * body : {"userName":"18750245119","password":"1A52C37FC53342A077CDDCD306F2695E","captcha":"","service":"http://localhost:8080/#/index/view"}
     */

    private String platform;
    private BodyBean body;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public BodyBean getBody() {
        return body;
    }

    public void setBody(BodyBean body) {
        this.body = body;
    }

    public static class BodyBean {
        /**
         * userName : 18750245119
         * password : 1A52C37FC53342A077CDDCD306F2695E
         * captcha :
         * service : http://localhost:8080/#/index/view
         */

        private String userName;
        private String password;
        private String captcha;
        private String service;
        private String userIdentity;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getCaptcha() {
            return captcha;
        }

        public void setCaptcha(String captcha) {
            this.captcha = captcha;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getUserIdentity() {
            return userIdentity;
        }

        public void setUserIdentity(String userIdentity) {
            this.userIdentity = userIdentity;
        }
    }
}
