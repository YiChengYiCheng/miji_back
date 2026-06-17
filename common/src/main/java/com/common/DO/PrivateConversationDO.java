package com.common.DO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("private_conversation")
public class PrivateConversationDO extends BaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userAId;

    private Long userBId;

    private Long lastMessageId;

    private String lastMessageContent;

    private LocalDateTime lastMessageTime;

    private Integer userAUnreadCount;

    private Integer userBUnreadCount;

    private Integer userADeleted;

    private Integer userBDeleted;
}
