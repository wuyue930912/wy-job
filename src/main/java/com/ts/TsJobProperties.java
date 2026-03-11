package com.ts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ts-job")
public class TsJobProperties {

    private Boolean enableBanner = false;

    private Boolean enableRecord = false;
    
    // 登录配置
    private Login login = new Login();
    
    // 告警配置
    private Alert alert = new Alert();
    
    // 调度配置
    private Scheduler scheduler = new Scheduler();
    
    @Data
    public static class Login {
        /** 是否启用登录验证 */
        private Boolean enabled = true;
        /** 用户名 */
        private String username = "admin";
        /** 密码 */
        private String password = "admin123";
    }

    @Data
    public static class Alert {
        /** 是否启用告警 */
        private Boolean enabled = true;
        /** 告警冷却时间（分钟） */
        private Integer cooldownMinutes = 30;
        /** 是否启用Webhook */
        private Boolean enableWebhook = false;
        /** Webhook URL */
        private String webhookUrl;
        /** Webhook请求方式 */
        private String webhookMethod = "POST";
        /** 是否启用企业微信告警 */
        private Boolean enableWechat = false;
        /** 企业微信Webhook KEY */
        private String wechatKey;
    }
    
    @Data
    public static class Scheduler {
        /** 默认最大并发数 */
        private Integer defaultMaxConcurrent = 1;
        /** 默认任务超时时间（秒，0表示不限制） */
        private Integer defaultTimeout = 0;
        /** 默认重试次数 */
        private Integer defaultRetryCount = 0;
        /** 是否启用任务依赖检查 */
        private Boolean enableDependencyCheck = true;
        /** 是否启用健康检查 */
        private Boolean enableHealthCheck = true;
    }

}
