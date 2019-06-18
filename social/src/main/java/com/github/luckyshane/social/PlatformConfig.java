package com.github.luckyshane.social;

/*
 * @author: luckyShane
 */
public class PlatformConfig {
    private String qqAppId;
    private String qqSecret;
    private String wechatAppId;
    private String wechatSecret;

    private PlatformConfig() {}

    public String getQQAppId() {
        return qqAppId;
    }

    public String getQQSecret() {
        return qqSecret;
    }

    public String getWechatAppId() {
        return wechatAppId;
    }

    public String getWechatSecret() {
        return wechatSecret;
    }

    public static class Builder {
        private String qqAppId;
        private String qqSecret;
        private String wechatAppId;
        private String wechatSecret;

        public Builder setQQ(String qqAppId, String qqSecret) {
            this.qqAppId = qqAppId;
            this.qqSecret = qqSecret;
            return this;
        }

        public Builder setWechat(String wechatAppId, String wechatSecret) {
            this.wechatAppId = wechatAppId;
            this.wechatSecret = wechatSecret;
            return this;
        }

        public PlatformConfig build() {
            PlatformConfig config = new PlatformConfig();
            config.qqAppId = qqAppId;
            config.qqSecret = qqSecret;
            config.wechatAppId = wechatAppId;
            config.wechatSecret = wechatSecret;
            return config;
        }

    }


}
