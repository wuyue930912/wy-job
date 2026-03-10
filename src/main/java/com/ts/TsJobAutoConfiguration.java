/**
 * TsJob 自动配置类
 * 
 * 该类用于在 Spring Boot 启动时，自动装配 wy-job 相关组件，包括：
 * - 加载调度任务相关配置（TsJobProperties）
 * - 扫描 com.ts 包下的所有 Spring 组件
 * - 注入 TsJobYmlProvider 实现，用于支持从 yml 文件加载任务配置
 * - 提供必要的 Bean 初始化，是整个调度框架的入口配置
 * 
 * @author yue.wu
 * @see TsJobProperties
 * @see TsJobYmlProvider
 */

import com.ts.provider.TsJobYmlProvider;
import com.ts.provider.TsJobYmlProviderImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TsJobProperties.class)
@ComponentScan("com.ts")
public class TsJobAutoConfiguration {

    private final TsJobProperties properties;

    /**
     * 构造方法，注入调度任务配置属性
     * 
     * @param properties 任务配置属性，来自 TsJobProperties，通常由 @EnableConfigurationProperties 注入
     */
    public TsJobAutoConfiguration(TsJobProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Order(0)
    public TsJobYmlProvider ymlTsJobProvider() {
        return new TsJobYmlProviderImpl(properties.getEnableRecord(), properties.getEnableBanner());
    }

}
