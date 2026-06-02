package com.common.enums;

public enum CodeEnum {
    // 成功
    OK(200, "success"),

    // 通用错误
    COMMON_ERROR(100, "通用错误"),
    COMMON_ERROR_WRONG_METHOD(101, "请求方法错误"),
    COMMON_ERROR_SERIALIZER_VALIDATION(102, "序列化错误：输入或者输出参数错误，没有被下面的类别捕捉到或者被提前抛出会报这个错误"),

    // 输入参数错误
    PARAM_ERROR(1000, "输入参数错误"),
    PARAM_ERROR_TYPE_MISMATCH(1001, "参数类型不匹配"),
    PARAM_ERROR_INVALID_VALUE(1002, "参数值无效"),
    PARAM_ERROR_NULL_VALUE(1003, "参数值为空"),
    PARAM_ERROR_INVALID_URL(1004, "参数是无效链接"),

    // 数据库操作错误
    CUSTOM_DATABASE_ERROR(2000, "数据库操作错误"),
    CUSTOM_DATABASE_ERROR_INSERT_FAIL(2001, "数据库插入失败"),
    CUSTOM_DATABASE_ERROR_UPDATE_FAIL(2002, "数据库更新失败");

    private final int statusCode;
    private final String msg;

    CodeEnum(int statusCode, String msg) {
            this.statusCode = statusCode;
            this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    // 根据 code 查找对应的 ErrorCode
    public static CodeEnum fromCode(int statusCode) {
        for (CodeEnum errorCode : CodeEnum.values()) {
            if (errorCode.statusCode == statusCode) {
                return errorCode;
            }
        }
        throw new IllegalArgumentException("未知错误码: " + statusCode);
    }

}
