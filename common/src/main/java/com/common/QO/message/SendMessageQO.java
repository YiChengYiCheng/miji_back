package com.common.QO.message;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Data
public class SendMessageQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "receiverUserId can not be null")
    private Long receiverUserId;

    @NotBlank(message = "content can not be blank")
    @Size(max = 1000, message = "content length can not exceed 1000")
    private String content;
}
