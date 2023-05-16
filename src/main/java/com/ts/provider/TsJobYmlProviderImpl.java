package com.ts.provider;

import java.util.HashMap;
import java.util.Map;

public class TsJobYmlProviderImpl implements TsJobYmlProvider {

    private final Boolean enableRecord;

    private final Boolean enableBanner;

    public TsJobYmlProviderImpl(Boolean enableRecord, Boolean enableBanner) {
        this.enableRecord = enableRecord;
        this.enableBanner = enableBanner;
    }

    @Override
    public Map<String, Object> ymlConfig() {
        Map<String, Object> map = new HashMap<>();
        map.put("enableRecord", this.enableRecord);
        map.put("enableBanner", this.enableBanner);
        return map;
    }

}
