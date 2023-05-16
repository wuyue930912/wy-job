package com.ts.vo;

import lombok.Data;

@Data
public class TsJobPageParamVO<T> {

    private Integer pageIndex = 1;
    private Integer pageSize = 1;
    private T searchData = null;

}
