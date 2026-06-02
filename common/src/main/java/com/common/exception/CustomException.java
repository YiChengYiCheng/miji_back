package com.common.exception;

import com.common.enums.CodeEnum;
import lombok.Data;

@Data
public class CustomException extends RuntimeException{
    private Integer status;

    public CustomException(String message){
        super(message);
        this.status= CodeEnum.COMMON_ERROR.getStatusCode();
    }
    public CustomException(Integer status, String message){
        super(message);
        this.status= status;
    }

}
