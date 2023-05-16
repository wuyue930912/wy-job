package com.ts.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TsJobPageResultVO<T> {

    private T result;
    private Long total;

}
