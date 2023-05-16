package com.ts;

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

    public TsJobAutoConfiguration(TsJobProperties properties) {
        this.properties = properties;
    }

    @Bean
    @Order(0)
    public TsJobYmlProvider ymlTsJobProvider() {
        return new TsJobYmlProviderImpl(properties.getEnableRecord(), properties.getEnableBanner());
    }

}
