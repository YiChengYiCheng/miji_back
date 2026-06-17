package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("private_message")
public class PrivateMessageDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long conversationId;

    private Long senderUserId;

    private Long receiverUserId;

    private String content;

    private Integer isRead;

    private Integer senderDeleted;

    private Integer receiverDeleted;
}
