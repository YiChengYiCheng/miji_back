package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 评论表
 */
@Data
@TableName("comment")
public class CommentDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 评论用户
     */
    private Long userId;

    /**
     * 父评论ID，一级评论为0
     */
    private Long parentId;

    /**
     * 一级评论ID，一级评论为0
     */
    private Long rootId;

    /**
     * 被回复用户ID
     */
    private Long replyUserId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;
}
