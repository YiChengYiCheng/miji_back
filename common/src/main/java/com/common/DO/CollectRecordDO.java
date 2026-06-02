package com.common.DO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏记录表
 */
@Data
@TableName("collect_record")
public class CollectRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;

}