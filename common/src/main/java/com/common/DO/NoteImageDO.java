package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记图片表
 */
@Data
@TableName("note_image")
public class NoteImageDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 笔记ID
     */
    private Long noteId;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 排序
     */
    private Integer sortNum;

    //创建时间
    private LocalDateTime createTime;

}