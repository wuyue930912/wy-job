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
    
    @Data
    public static class Login {
        /** 是否启用登录验证 */
        private Boolean enabled = true;
        /** 用户名 */
        private String username = "admin";
        /** 密码 */
        private String password = "admin123";
    }

}
