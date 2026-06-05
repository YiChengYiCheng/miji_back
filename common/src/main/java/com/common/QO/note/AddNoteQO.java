package com.common.QO.note;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class AddNoteQO implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户id不能为空")
    private Long userId;

    @NotNull(message = "标题不能为空")
    private String title;

    @NotNull(message = "内容不能为空")
    private String content;

    private String cover;

    private List<String> images;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer commentCount;
}
