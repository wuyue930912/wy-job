package com.ts.vo;

import lombok.Data;

/**
 * @author yue.wu
 * @description 通用下行参数
 * @date 2022/4/1 14:09
 */
@Data
public class TsJobResponseVO<T> {

    private Long code;
    private String msg;
    private T data;

    public TsJobResponseVO(Long code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public TsJobResponseVO(Long code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
