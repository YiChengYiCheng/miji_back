package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录表
 */
@Data
@TableName("like_record")
public class LikeRecordDO {

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
     * 点赞时间
     */
    private LocalDateTime createTime;

}