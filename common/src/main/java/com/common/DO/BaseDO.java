package com.common.DO;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseDO {

    //创建时间
    private LocalDateTime createTime;
    //更改时间
    private LocalDateTime updateTime;
    //状态值
    private Integer status;
}
