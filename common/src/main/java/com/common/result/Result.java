package com.common.result;

import com.common.enums.CodeEnum;
import lombok.Data;

import java.io.Serializable;

@Data
//定义返回类
public class Result<T> implements Serializable {
    private Integer status;
    private String msg;
    private T data;

    //成功返回
    public static Result success(Object data) {
        Result result = new Result();
        result.setData(data);
        result.setStatus(CodeEnum.OK.getStatusCode());
        result.setMsg(CodeEnum.OK.getMsg());
        return result;
    }
    public static Result success(Object data,Integer errorCode,String msg) {
        Result result = new Result();
        result.setData(data);
        result.setStatus(errorCode);
        result.setMsg(msg);
        return result;
    }

    //失败返回
    public static Result fail(Integer errorCode,String msg) {
        Result result = new Result();
        result.setData(null);
        result.setStatus(errorCode);
        result.setMsg(msg);
        return result;
    }
}
