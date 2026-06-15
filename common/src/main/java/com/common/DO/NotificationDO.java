package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("notification")
public class NotificationDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long receiverUserId;

    private Long senderUserId;

    private Integer type;

    private Long noteId;

    private Long commentId;

    private String content;

    private Integer isRead;
}
