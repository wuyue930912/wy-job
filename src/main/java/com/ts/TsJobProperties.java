package com.ts;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ts-job")
public class TsJobProperties {

    private Boolean enableBanner = false;

    private Boolean enableRecord = false;

}
